package com.atomicbunker.reservation.dto;

import com.atomicbunker.reservation.domain.Seat;
import com.atomicbunker.reservation.domain.SeatStatus;

public record SeatDTO(
        String id,
        String seatRow,
        Integer seatColumn,
        String status
) {
    public static SeatDTO from(Seat seat) {
        return new SeatDTO(
                seat.id.toString(),
                seat.seatRow,
                seat.seatColumn,
                seat.status.name()
        );
    }
}
