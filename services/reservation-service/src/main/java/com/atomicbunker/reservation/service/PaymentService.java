package com.atomicbunker.reservation.service;

import com.atomicbunker.reservation.domain.Seat;
import com.atomicbunker.reservation.domain.SeatRepository;
import com.atomicbunker.reservation.domain.SeatStatus;
import com.atomicbunker.reservation.dto.PaymentResponse;
import com.atomicbunker.reservation.exception.HoldExpiredException;
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
import org.hibernate.reactive.mutiny.Mutiny;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class PaymentService {

    private final SeatRepository seatRepository;
    private final SeatHoldService seatHoldService;
    private final SeatWebSocket seatWebSocket;
    private final ObjectMapper objectMapper;
    /**
     * Injected to open an independent session+transaction for expired-hold
     * releases.
     * 
     * @WithTransaction on releaseSeatHold() would join the current outer
     *                  transaction
     *                  (REQUIRED semantics) and be rolled back together with the
     *                  HoldExpiredException.
     *                  Using sessionFactory.withTransaction() starts a brand-new
     *                  session that commits
     *                  independently, ensuring the seat is freed even though the
     *                  outer call fails.
     */
    private final Mutiny.SessionFactory sessionFactory;

    @WithTransaction
    public Uni<PaymentResponse> processPayment(UUID reservationId, String sessionId, String email, String phone) {
        return seatRepository.findByReservationIdWithLock(reservationId)
                .onFailure(PessimisticLockException.class)
                .recoverWithUni(e -> Uni.createFrom().failure(
                        new jakarta.ws.rs.WebApplicationException(
                                "Seat is currently being processed by another request, please retry", 503)))
                .chain(seat -> {
                    if (seat == null) {
                        return Uni.createFrom().failure(
                                new jakarta.ws.rs.NotFoundException("Reservation not found"));
                    }

                    if (seat.getStatus() == SeatStatus.SOLD) {
                        return Uni.createFrom().failure(
                                new SeatAlreadyTakenException("Seat has already been sold"));
                    }

                    if (seat.getStatus() != SeatStatus.HELD) {
                        return Uni.createFrom().failure(
                                new SeatAlreadyTakenException("Seat is not in HELD status"));
                    }

                    // Ownership check: missing or wrong X-Session-ID → 403
                    if (sessionId == null || !sessionId.equals(seat.getHeldBy())) {
                        return Uni.createFrom().failure(
                                new jakarta.ws.rs.ForbiddenException("You do not own this reservation"));
                    }

                    if (seat.isHoldExpired()) {
                        // Release the hold in a BRAND-NEW independent session+transaction so it
                        // commits regardless of this outer @WithTransaction being rolled back.
                        // Calling seatHoldService.releaseSeatHold() would join the current outer
                        // transaction (@WithTransaction = REQUIRED) and be rolled back together
                        // with the HoldExpiredException, leaving the seat stuck in HELD state.
                        UUID seatId = seat.getId();
                        String heldBy = seat.getHeldBy();
                        return sessionFactory.withTransaction(
                                (s, tx) -> s.find(Seat.class, seatId)
                                        .chain(toRelease -> {
                                            if (toRelease == null) {
                                                return Uni.createFrom().voidItem();
                                            }
                                            toRelease.setStatus(SeatStatus.AVAILABLE);
                                            toRelease.setHeldAt(null);
                                            toRelease.setHeldBy(null);
                                            toRelease.setReservationId(null);
                                            return s.persist(toRelease).replaceWithVoid();
                                        }))
                                .chain(() -> Uni.createFrom().failure(
                                        new HoldExpiredException("Your hold has expired. Please select a new seat.")));
                    }

                    // === MOCK PAYMENT PROCESSING ===
                    // In production, call external payment gateway here
                    // For now, always succeed

                    // Transition to SOLD (NFR6: IMMUTABLE — this is the only code path to SOLD)
                    seat.setStatus(SeatStatus.SOLD);
                    seat.setEmail(email);
                    seat.setPhone(phone);
                    seat.setPaidAt(Instant.now());

                    // flush() before broadcast: surface any DB constraint violations
                    // (e.g. unique violation) before sending the WebSocket notification,
                    // so clients never receive a SEAT_SOLD event for a failed transaction.
                    return seatRepository.persist(seat)
                            .call(s -> seatRepository.flush())
                            .chain(updatedSeat -> {
                                broadcastSeatSold(updatedSeat);
                                return Uni.createFrom().item(createPaymentResponse(updatedSeat));
                            });
                });
    }

    private void broadcastSeatSold(Seat seat) {
        try {
            ObjectNode message = objectMapper.createObjectNode();
            message.put("type", "SEAT_SOLD");
            message.put("seatId", seat.getId().toString());
            message.put("status", seat.getStatus().name());
            message.put("reservationId", seat.getReservationId().toString());

            // Use showtimeId (read-only FK mirror on Seat) — seat.getShowtime() is
            // FetchType.LAZY and is not initialised by the native SQL query, causing a
            // LazyInitializationException if dereferenced here.
            seatWebSocket.broadcastSeatUpdate(
                    seat.getShowtimeId().toString(),
                    message);
        } catch (Exception e) {
            log.error("Failed to broadcast seat sold for seat {}", seat.getId(), e);
        }
    }

    private PaymentResponse createPaymentResponse(Seat seat) {
        String confirmationNumber = "CNF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return PaymentResponse.builder()
                .reservationId(seat.getReservationId())
                .status(seat.getStatus())
                .seatId(seat.getId())
                .confirmationNumber(confirmationNumber)
                .build();
    }
}
