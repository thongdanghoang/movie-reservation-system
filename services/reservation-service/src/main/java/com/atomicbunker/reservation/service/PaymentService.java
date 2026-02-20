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
                        // Delegate release to SeatHoldService which runs in its OWN @WithTransaction.
                        // This ensures the AVAILABLE status update is committed in a separate
                        // transaction BEFORE this outer transaction emits the failure Uni.
                        // Using persistAndFlush() here wouldn't help: @WithTransaction rolls back
                        // the whole outer transaction (including the flush) when the Uni fails.
                        return seatHoldService.releaseSeatHold(seat.getId(), seat.getHeldBy())
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

                    return seatRepository.persist(seat)
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

            seatWebSocket.broadcastSeatUpdate(
                    seat.getShowtime().getId().toString(),
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
