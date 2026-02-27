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
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@ApplicationScoped
public class ExpiredHoldCleanupJob {

    private static final Logger LOG = Logger.getLogger(ExpiredHoldCleanupJob.class);
    private static final int HOLD_TIMEOUT_MINUTES = 5;
    private static final String EVENT_TYPE_HOLD_EXPIRED = "HOLD_EXPIRED";

    @Inject
    SeatRepository seatRepository;

    @Inject
    SeatWebSocket seatWebSocket;

    @Scheduled(cron = "0 */1 * * * ?")
    @WithSession
    public Uni<Void> cleanupExpiredHolds(ScheduledExecution execution) {
        LOG.infof("Starting expired hold cleanup job");

        Instant expirationThreshold = Instant.now().minus(HOLD_TIMEOUT_MINUTES, ChronoUnit.MINUTES);

        return seatRepository.findExpiredHolds(expirationThreshold)
            .flatMap(this::processExpiredHolds)
            .onFailure().recoverWithUni(t -> {
                LOG.errorf("Failed to process expired holds: %s", t.getMessage());
                return Uni.createFrom().voidItem();
            });
    }

    private Uni<Void> processExpiredHolds(List<Seat> expiredSeats) {
        if (expiredSeats == null || expiredSeats.isEmpty()) {
            LOG.debug("No expired holds found");
            return Uni.createFrom().voidItem();
        }

        LOG.infof("Found %d expired holds to process", expiredSeats.size());

        Uni<Void> result = Uni.createFrom().voidItem();
        for (Seat seat : expiredSeats) {
            result = result.flatMap(v -> processExpiredHold(seat));
        }
        
        return result.map(v -> {
            LOG.infof("Completed expired hold cleanup - processed %d seats", expiredSeats.size());
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
                    LOG.debugf("Seat %s is no longer held, skipping", lockedSeat.getId());
                    return Uni.createFrom().voidItem();
                }

                if (lockedSeat.getHeldAt() != null && 
                    lockedSeat.getHeldAt().plus(HOLD_TIMEOUT_MINUTES, ChronoUnit.MINUTES).isAfter(Instant.now())) {
                    LOG.debugf("Seat %s hold has not yet expired", lockedSeat.getId());
                    return Uni.createFrom().voidItem();
                }

                Instant now = Instant.now();
                lockedSeat.setStatus(SeatStatus.AVAILABLE);
                lockedSeat.setHeldAt(null);
                lockedSeat.setHeldBy(null);
                lockedSeat.setReservationId(null);
                lockedSeat.setEmail(null);
                lockedSeat.setPhone(null);

                return seatRepository.persist(lockedSeat)
                    .map(updated -> {
                        LOG.infof("Released expired hold for seat %s", updated.getId());
                        broadcastSeatUpdate(updated);
                        createAuditLog(seat, now);
                        return null;
                    });
            })
            .onFailure().recoverWithUni(t -> {
                LOG.warnf("Failed to process seat %s: %s", seat.getId(), t.getMessage());
                return Uni.createFrom().voidItem();
            });
    }

    private void createAuditLog(Seat seat, Instant timestamp) {
        try {
            AuditLog auditLog = new AuditLog();
            auditLog.reservationId = seat.getReservationId() != null ? seat.getReservationId().toString() : null;
            auditLog.seatId = seat.getId().toString();
            auditLog.showtimeId = seat.getShowtimeId() != null ? seat.getShowtimeId().toString() : null;
            auditLog.eventType = EVENT_TYPE_HOLD_EXPIRED;
            auditLog.timestamp = timestamp;
            auditLog.metadata = String.format("Seat hold expired after %d minutes. Row: %s, Column: %d", 
                HOLD_TIMEOUT_MINUTES, seat.getSeatRow(), seat.getSeatColumn());
            
            auditLog.persist();
            LOG.debugf("Created audit log for expired hold: reservation=%s, seat=%s", 
                auditLog.reservationId, auditLog.seatId);
        } catch (Exception e) {
            LOG.warnf("Failed to create audit log: %s", e.getMessage());
        }
    }

    private void broadcastSeatUpdate(Seat seat) {
        if (seat.getShowtimeId() != null) {
            SeatUpdatePayload payload = new SeatUpdatePayload(
                seat.getId().toString(),
                seat.getShowtimeId().toString(),
                seat.getSeatRow(),
                seat.getSeatColumn(),
                SeatStatus.AVAILABLE.name()
            );
            seatWebSocket.broadcastSeatUpdate(seat.getShowtimeId().toString(), payload);
        }
    }

    public static class SeatUpdatePayload {
        private String seatId;
        private String showtimeId;
        private String row;
        private Integer column;
        private String status;

        public SeatUpdatePayload() {}

        public SeatUpdatePayload(String seatId, String showtimeId, String row, Integer column, String status) {
            this.seatId = seatId;
            this.showtimeId = showtimeId;
            this.row = row;
            this.column = column;
            this.status = status;
        }

        public String getSeatId() { return seatId; }
        public void setSeatId(String seatId) { this.seatId = seatId; }
        public String getShowtimeId() { return showtimeId; }
        public void setShowtimeId(String showtimeId) { this.showtimeId = showtimeId; }
        public String getRow() { return row; }
        public void setRow(String row) { this.row = row; }
        public Integer getColumn() { return column; }
        public void setColumn(Integer column) { this.column = column; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
}