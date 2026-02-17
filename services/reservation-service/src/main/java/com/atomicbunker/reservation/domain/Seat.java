package com.atomicbunker.reservation.domain;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.UUID;

@Entity
@Table(name = "seats")
public class Seat extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    @NotNull(message = "Showtime is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "showtime_id", nullable = false)
    public Showtime showtime;

    @NotBlank(message = "Seat row is required")
    @Column(name = "seat_row", nullable = false)
    public String seatRow;

    @NotNull(message = "Seat column is required")
    @Positive(message = "Seat column must be positive")
    @Column(name = "seat_column", nullable = false)
    public Integer seatColumn;

    @NotNull(message = "Seat status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    public SeatStatus status = SeatStatus.AVAILABLE;
}
