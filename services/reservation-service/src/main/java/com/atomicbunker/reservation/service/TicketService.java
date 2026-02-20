package com.atomicbunker.reservation.service;

import com.atomicbunker.reservation.domain.SeatRepository;
import com.atomicbunker.reservation.domain.SeatStatus;
import com.atomicbunker.reservation.dto.TicketResponse;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.WebApplicationException;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@ApplicationScoped
@RequiredArgsConstructor
public class TicketService {

    private final SeatRepository seatRepository;

    public Uni<TicketResponse> getTicket(UUID reservationId, String sessionId) {
        return seatRepository.findByReservationId(reservationId)
                .onItem().ifNull().failWith(() -> new WebApplicationException("Reservation not found", 404))
                .chain(seat -> {
                    // Ownership check: Session ID must match the holder
                    if (sessionId == null || !sessionId.equals(seat.getHeldBy())) {
                        return Uni.createFrom()
                                .failure(new WebApplicationException("You do not own this reservation", 403));
                    }

                    if (seat.getStatus() != SeatStatus.SOLD) {
                        return Uni.createFrom()
                                .failure(new WebApplicationException("Ticket not available yet", 404));
                    }

                    // TODO: Replace with real JWT/HMAC signing (see Story 4.1).
                    // This intentionally produces a placeholder string
                    String mockToken = "mock-ticket-" + reservationId;

                    return Uni.createFrom().item(new TicketResponse(
                            seat.getReservationId(),
                            seat.getId(),
                            seat.getStatus(),
                            seat.getEmail(),
                            mockToken,
                            seat.getPaidAt()));
                });
    }
}
