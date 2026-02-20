package com.atomicbunker.reservation.dto;

import com.atomicbunker.reservation.domain.SeatStatus;
import lombok.Builder;
import java.util.UUID;

@Builder
public record PaymentResponse(
        UUID reservationId,
        SeatStatus status,
        UUID seatId,
        String confirmationNumber) {
}
