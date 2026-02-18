package com.atomicbunker.reservation.dto;

public record SeatDTO(
        String id,
        String seatRow,
        Integer seatColumn,
        String status
) {}
