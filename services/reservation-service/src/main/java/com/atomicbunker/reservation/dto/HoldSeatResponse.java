package com.atomicbunker.reservation.dto;

import com.atomicbunker.reservation.domain.SeatStatus;
import lombok.Builder;
import java.time.Instant;
import java.util.UUID;

@Builder
public record HoldSeatResponse(
        UUID seatId,
        SeatStatus status,
        Instant heldAt,
        Instant holdExpiresAt,
        UUID reservationId,
        String sessionId // echoed from the request so the client can use it as X-Session-ID on payment
) {
}
