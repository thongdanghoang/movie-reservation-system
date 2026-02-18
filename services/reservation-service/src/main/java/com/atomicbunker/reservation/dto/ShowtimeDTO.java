package com.atomicbunker.reservation.dto;

import java.time.Instant;
import java.util.UUID;

public record ShowtimeDTO(
    UUID id,
    UUID movieId,
    String movieTitle,
    Instant startTime,
    String theaterName,
    Integer availableSeats
) {}
