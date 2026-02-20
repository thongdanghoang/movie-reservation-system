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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Entity
@Table(name = "seats")
@Getter
@Setter
@NoArgsConstructor
public class Seat extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull(message = "Showtime is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "showtime_id", nullable = false)
    private Showtime showtime;

    /**
     * Read-only mirror of the showtime_id FK column.
     * Allows access to the showtime UUID without triggering lazy-loading of the
     * Showtime proxy — critical for WebSocket broadcasts after native SQL queries.
     */
    @Column(name = "showtime_id", insertable = false, updatable = false)
    private UUID showtimeId;

    @NotBlank(message = "Seat row is required")
    @Column(name = "seat_row", nullable = false)
    private String seatRow;

    @NotNull(message = "Seat column is required")
    @Positive(message = "Seat column must be positive")
    @Column(name = "seat_column", nullable = false)
    private Integer seatColumn;

    @NotNull(message = "Seat status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SeatStatus status = SeatStatus.AVAILABLE;

    @Column(name = "held_at")
    private Instant heldAt;

    @Column(name = "held_by")
    private String heldBy;

    @Column(name = "reservation_id")
    private UUID reservationId;

    @Column(name = "email")
    private String email;

    @Column(name = "phone")
    private String phone;

    @Column(name = "paid_at")
    private Instant paidAt;

    public boolean isHoldExpired() {
        if (heldAt == null)
            return false;
        return Instant.now().isAfter(heldAt.plus(5, ChronoUnit.MINUTES));
    }
}
