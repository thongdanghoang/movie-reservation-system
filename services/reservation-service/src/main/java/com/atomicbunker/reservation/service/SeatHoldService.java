package com.atomicbunker.reservation.service;

import com.atomicbunker.reservation.domain.Seat;
import com.atomicbunker.reservation.domain.SeatRepository;
import com.atomicbunker.reservation.domain.SeatStatus;
import com.atomicbunker.reservation.dto.HoldSeatResponse;
import com.atomicbunker.reservation.exception.SeatAlreadyTakenException;
import com.atomicbunker.reservation.websocket.SeatWebSocket;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.PessimisticLockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class SeatHoldService {

    private final SeatRepository seatRepository;
    private final SeatWebSocket seatWebSocket;
    private final ObjectMapper objectMapper;

    @WithTransaction
    public Uni<HoldSeatResponse> holdSeat(UUID seatId, String sessionId) {
        log.info("Attempting to hold seat {} for session {}", seatId, sessionId);

        return seatRepository.findByIdWithLock(seatId)
                .chain(seat -> {
                    if (seat == null) {
                        log.warn("Seat {} not found", seatId);
                        return Uni.createFrom().failure(
                                new jakarta.ws.rs.NotFoundException("Seat not found"));
                    }

                    // Check if sold or held (and not expired)
                    if (seat.getStatus() == SeatStatus.SOLD) {
                        log.warn("Seat {} is already sold", seatId);
                        return Uni.createFrom().failure(
                                new SeatAlreadyTakenException("Seat has been sold"));
                    }
                    if (seat.getStatus() == SeatStatus.HELD && !seat.isHoldExpired()) {
                        log.warn("Seat {} is held by another user (expires at {})",
                                seatId, seat.getHeldAt().plus(5, ChronoUnit.MINUTES));
                        return Uni.createFrom().failure(
                                new SeatAlreadyTakenException("Seat is being held by another user"));
                    }
                    // If status is HELD but expired, we can reclaim it - continue with hold

                    // Hold the seat
                    UUID reservationId = UUID.randomUUID();
                    Instant heldAt = Instant.now();

                    seat.setStatus(SeatStatus.HELD);
                    seat.setHeldAt(heldAt);
                    seat.setHeldBy(sessionId);
                    seat.setReservationId(reservationId);

                    log.info("Seat {} held successfully with reservation {}", seatId, reservationId);

                    return seatRepository.persist(seat)
                            .chain(updatedSeat -> {
                                // Broadcast to all connected clients
                                broadcastSeatHold(updatedSeat);
                                return Uni.createFrom().item(createHoldResponse(updatedSeat));
                            });
                })
                .onFailure(PessimisticLockException.class)
                .transform(e -> {
                    log.warn("Pessimistic lock conflict for seat {}", seatId);
                    return new SeatAlreadyTakenException("Seat is being held by another user");
                });
    }

    private void broadcastSeatHold(Seat seat) {
        try {
            ObjectNode message = objectMapper.createObjectNode();
            message.put("type", "SEAT_HELD");
            message.put("seatId", seat.getId().toString());
            message.put("status", seat.getStatus().name());
            message.put("heldBy", seat.getHeldBy());
            message.put("reservationId", seat.getReservationId().toString());
            message.put("heldAt", seat.getHeldAt().toString());

            seatWebSocket.broadcastSeatUpdate(
                    seat.getShowtime().getId().toString(),
                    message);
        } catch (Exception e) {
            log.error("Failed to broadcast seat hold for seat {}", seat.getId(), e);
        }
    }

    private HoldSeatResponse createHoldResponse(Seat seat) {
        return HoldSeatResponse.builder()
                .seatId(seat.getId())
                .status(seat.getStatus())
                .heldAt(seat.getHeldAt())
                .holdExpiresAt(seat.getHeldAt().plus(5, ChronoUnit.MINUTES))
                .reservationId(seat.getReservationId())
                .sessionId(seat.getHeldBy()) // echo back so client can use as X-Session-ID on payment
                .build();
    }

    @WithTransaction
    public Uni<Void> releaseSeatHold(UUID seatId, String sessionId) {
        log.info("Attempting to release hold for seat {}", seatId);

        return seatRepository.findByIdWithLock(seatId)
                .chain(seat -> {
                    if (seat == null) {
                        log.warn("Seat {} not found", seatId);
                        return Uni.createFrom().failure(
                                new jakarta.ws.rs.NotFoundException("Seat not found"));
                    }

                    if (seat.getStatus() != SeatStatus.HELD) {
                        log.info("Seat {} is not held, nothing to release", seatId);
                        return Uni.createFrom().nullItem();
                    }

                    // Ownership check: only the session that placed the hold can release it
                    if (sessionId == null || !sessionId.equals(seat.getHeldBy())) {
                        log.warn("Session {} attempted to release seat {} owned by {}",
                                sessionId, seatId, seat.getHeldBy());
                        return Uni.createFrom().failure(
                                new jakarta.ws.rs.ForbiddenException("You do not own this hold"));
                    }

                    seat.setStatus(SeatStatus.AVAILABLE);
                    seat.setHeldAt(null);
                    seat.setHeldBy(null);
                    seat.setReservationId(null);

                    log.info("Seat {} hold released successfully", seatId);

                    return seatRepository.persist(seat)
                            .chain(updatedSeat -> {
                                broadcastSeatHoldReleased(updatedSeat);
                                return Uni.createFrom().nullItem();
                            });
                })
                .replaceWithVoid();
    }

    private void broadcastSeatHoldReleased(Seat seat) {
        try {
            ObjectNode message = objectMapper.createObjectNode();
            message.put("type", "SEAT_RELEASED");
            message.put("seatId", seat.getId().toString());
            message.put("status", seat.getStatus().name());

            seatWebSocket.broadcastSeatUpdate(
                    seat.getShowtime().getId().toString(),
                    message);
        } catch (Exception e) {
            log.error("Failed to broadcast seat hold release for seat {}", seat.getId(), e);
        }
    }
}
