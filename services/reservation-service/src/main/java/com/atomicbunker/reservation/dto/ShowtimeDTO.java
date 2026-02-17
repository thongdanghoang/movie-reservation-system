package com.atomicbunker.reservation.dto;

import java.time.Instant;
import java.util.UUID;

public class ShowtimeDTO {
    public UUID id;
    public UUID movieId;
    public String movieTitle;
    public Instant startTime;
    public String theaterName;
    public Integer availableSeats;

    public ShowtimeDTO(UUID id, UUID movieId, String movieTitle, Instant startTime, String theaterName, Integer availableSeats) {
        this.id = id;
        this.movieId = movieId;
        this.movieTitle = movieTitle;
        this.startTime = startTime;
        this.theaterName = theaterName;
        this.availableSeats = availableSeats;
    }
}
