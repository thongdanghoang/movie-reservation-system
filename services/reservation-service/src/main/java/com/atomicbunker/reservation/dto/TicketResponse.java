package com.atomicbunker.reservation.dto;

import com.atomicbunker.reservation.domain.SeatStatus;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
public record TicketResponse(
        UUID reservationId,
        UUID seatId,
        SeatStatus status,
        String email,
        String signedTicketToken,
        Instant paidAt
) {
}
