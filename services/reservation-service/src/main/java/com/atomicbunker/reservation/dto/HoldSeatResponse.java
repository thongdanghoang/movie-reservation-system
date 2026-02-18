package com.atomicbunker.reservation.dto;

import com.atomicbunker.reservation.domain.SeatStatus;
import lombok.Builder;
import lombok.Data;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class HoldSeatResponse {
    
    private UUID seatId;
    private SeatStatus status;
    private Instant heldAt;
    private Instant holdExpiresAt;
    private UUID reservationId;
}
