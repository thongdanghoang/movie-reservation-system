package com.atomicbunker.reservation.domain;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "showtimes")
public class Showtime extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    @NotNull(message = "Movie is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id", nullable = false)
    public Movie movie;

    @NotNull(message = "Start time is required")
    @Column(name = "start_time", nullable = false)
    public Instant startTime;

    @NotBlank(message = "Theater name is required")
    @Column(name = "theater_name", nullable = false)
    public String theaterName;

    @NotNull(message = "Available seats is required")
    @PositiveOrZero(message = "Available seats must be non-negative")
    @Column(name = "available_seats", nullable = false)
    public Integer availableSeats;
}
