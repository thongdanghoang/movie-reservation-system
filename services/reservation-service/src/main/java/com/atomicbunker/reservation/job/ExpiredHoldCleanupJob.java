package com.atomicbunker.reservation.job;

import com.atomicbunker.reservation.domain.Seat;
import com.atomicbunker.reservation.domain.SeatRepository;
import com.atomicbunker.reservation.domain.SeatStatus;
import com.atomicbunker.reservation.domain.audit.AuditLog;
import com.atomicbunker.reservation.websocket.SeatWebSocket;
import io.quarkus.scheduler.Scheduled;
import io.quarkus.scheduler.ScheduledExecution;
import io.smallrye.mutiny.Uni;
import io.quarkus.hibernate.reactive.panache.common.WithSession;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class ExpiredHoldCleanupJob {

    private static final String EVENT_TYPE_HOLD_EXPIRED = "HOLD_EXPIRED";

    private final SeatRepository seatRepository;
    private final SeatWebSocket seatWebSocket;

    @ConfigProperty(name = "app.hold.timeout-minutes", defaultValue = "5")
    int holdTimeoutMinutes;

    @ConfigProperty(name = "app.hold.cleanup-cron", defaultValue = "0 */1 * * * ?")
    String cleanupCron;

    @ConfigProperty(name = "app.hold.cleanup-enabled", defaultValue = "true")
    boolean cleanupEnabled;

    @Scheduled(cron = "${app.hold.cleanup-cron}")
    @WithSession
    public Uni<Void> cleanupExpiredHolds(ScheduledExecution execution) {
        if (!cleanupEnabled) {
            log.debug("Cleanup job is disabled");
            return Uni.createFrom().voidItem();
        }

        log.info("Starting expired hold cleanup job");

        var expirationThreshold = Instant.now().minus(holdTimeoutMinutes, ChronoUnit.MINUTES);

        return seatRepository.findExpiredHolds(expirationThreshold)
            .flatMap(this::processExpiredHolds)
            .onFailure().recoverWithUni(t -> {
                log.error("Failed to process expired holds: {}", t.getMessage());
                return Uni.createFrom().voidItem();
            });
    }

    private Uni<Void> processExpiredHolds(java.util.List<Seat> expiredSeats) {
        if (CollectionUtils.isEmpty(expiredSeats)) {
            log.debug("No expired holds found");
            return Uni.createFrom().voidItem();
        }

        log.info("Found {} expired holds to process", expiredSeats.size());

        Uni<Void> result = Uni.createFrom().voidItem();
        for (var seat : expiredSeats) {
            result = result.flatMap(v -> processExpiredHold(seat));
        }
        
        return result.map(v -> {
            log.info("Completed expired hold cleanup - processed {} seats", expiredSeats.size());
            return null;
        });
    }

    private Uni<Void> processExpiredHold(Seat seat) {
        return seatRepository.findByIdWithLock(seat.getId())
            .flatMap(lockedSeat -> {
                if (lockedSeat == null) {
                    return Uni.createFrom().voidItem();
                }

                if (lockedSeat.getStatus() != SeatStatus.HELD) {
                    log.debug("Seat {} is no longer held, skipping", lockedSeat.getId());
                    return Uni.createFrom().voidItem();
                }

                if (lockedSeat.getHeldAt() != null && 
                    lockedSeat.getHeldAt().plus(holdTimeoutMinutes, ChronoUnit.MINUTES).isAfter(Instant.now())) {
                    log.debug("Seat {} hold has not yet expired", lockedSeat.getId());
                    return Uni.createFrom().voidItem();
                }

                var now = Instant.now();
                var reservationIdForAudit = lockedSeat.getReservationId();
                var showtimeIdForAudit = lockedSeat.getShowtimeId();
                var seatRowForAudit = lockedSeat.getSeatRow();
                var seatColumnForAudit = lockedSeat.getSeatColumn();

                lockedSeat.setStatus(SeatStatus.AVAILABLE);
                lockedSeat.setHeldAt(null);
                lockedSeat.setHeldBy(null);
                lockedSeat.setReservationId(null);
                lockedSeat.setEmail(null);
                lockedSeat.setPhone(null);

                return seatRepository.persist(lockedSeat)
                    .flatMap(updated -> {
                        log.info("Released expired hold for seat {}", updated.getId());
                        broadcastSeatUpdate(updated);
                        return createAuditLog(updated.getId(), reservationIdForAudit, showtimeIdForAudit,
                                seatRowForAudit, seatColumnForAudit, now);
                    });
            })
            .onFailure().recoverWithUni(t -> {
                log.warn("Failed to process seat {}: {}", seat.getId(), t.getMessage());
                return Uni.createFrom().voidItem();
            });
    }

    private Uni<Void> createAuditLog(UUID seatId, UUID reservationId, UUID showtimeId, 
                                     String seatRow, Integer seatColumn, Instant timestamp) {
        var auditLog = new AuditLog();
        auditLog.setReservationId(reservationId != null ? reservationId.toString() : null);
        auditLog.setSeatId(seatId.toString());
        auditLog.setShowtimeId(showtimeId != null ? showtimeId.toString() : null);
        auditLog.setEventType(EVENT_TYPE_HOLD_EXPIRED);
        auditLog.setTimestamp(timestamp);
        auditLog.setMetadata(String.format("Seat hold expired after %d minutes. Row: %s, Column: %d", 
            holdTimeoutMinutes, seatRow, seatColumn));
        
        return auditLog.persist()
            .chain(a -> {
                log.debug("Created audit log for expired hold: reservation={}, seat={}", 
                    auditLog.getReservationId(), auditLog.getSeatId());
                return Uni.createFrom().voidItem();
            })
            .onFailure().recoverWithUni(t -> {
                log.warn("Failed to create audit log: {}", t.getMessage());
                return Uni.createFrom().voidItem();
            });
    }

    private void broadcastSeatUpdate(Seat seat) {
        if (seat.getShowtimeId() != null) {
            var payload = new SeatUpdatePayload(
                seat.getId().toString(),
                seat.getShowtimeId().toString(),
                seat.getSeatRow(),
                seat.getSeatColumn(),
                SeatStatus.AVAILABLE.name()
            );
            seatWebSocket.broadcastSeatUpdate(seat.getShowtimeId().toString(), payload);
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SeatUpdatePayload {
        private String seatId;
        private String showtimeId;
        private String row;
        private Integer column;
        private String status;
    }
}
