package com.atomicbunker.reservation.service;

import com.atomicbunker.reservation.domain.SeatRepository;
import com.atomicbunker.reservation.domain.SeatStatus;
import com.atomicbunker.reservation.dto.TicketResponse;
import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.NotFoundException;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@ApplicationScoped
@RequiredArgsConstructor
public class TicketService {

    private final SeatRepository seatRepository;

    @WithSession
    public Uni<TicketResponse> getTicket(UUID reservationId) {
        return seatRepository.findByReservationId(reservationId)
                .chain(seat -> {
                    if (seat == null) {
                        return Uni.createFrom().failure(new NotFoundException("Reservation not found"));
                    }
                    if (seat.getStatus() != SeatStatus.SOLD) {
                        return Uni.createFrom()
                                .failure(new NotFoundException("Ticket not available yet"));
                    }

                    // Mock a signed token - e.g. JWT or HMAC string
                    String mockToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.mock-" + reservationId.toString();

                    return Uni.createFrom().item(TicketResponse.builder()
                            .reservationId(seat.getReservationId())
                            .seatId(seat.getId())
                            .status(seat.getStatus())
                            .email(seat.getEmail())
                            .signedTicketToken(mockToken)
                            .paidAt(seat.getPaidAt())
                            .build());
                });
    }
}
