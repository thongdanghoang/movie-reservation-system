package com.atomicbunker.reservation.domain;

import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.LockModeType;
import org.hibernate.jpa.SpecHints;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class SeatRepository implements PanacheRepositoryBase<Seat, UUID> {

    public Uni<List<Seat>> findByShowtimeId(UUID showtimeId) {
        return list("showtime.id", showtimeId);
    }

    public Uni<Long> countByShowtimeIdAndStatus(UUID showtimeId, SeatStatus status) {
        return count("showtime.id = ?1 and status = ?2", showtimeId, status);
    }

    /**
     * Find seat by ID with pessimistic write lock (FOR UPDATE NOWAIT).
     * Fails immediately if seat is already locked.
     */
    public Uni<Seat> findByIdWithLock(UUID seatId) {
        return getSession()
            .chain(session -> {
                return session.find(Seat.class, seatId)
                    .chain(seat -> {
                        if (seat == null) {
                            return Uni.createFrom().nullItem();
                        }
                        return session.lock(seat, LockModeType.PESSIMISTIC_WRITE)
                            .map(v -> seat);
                    });
            });
    }

    /**
     * Find available seat by ID. Returns empty if seat doesn't exist or is not available.
     */
    public Uni<Optional<Seat>> findAvailableById(UUID seatId) {
        return findById(seatId)
            .map(seat -> {
                if (seat == null || seat.getStatus() != SeatStatus.AVAILABLE) {
                    return Optional.empty();
                }
                return Optional.of(seat);
            });
    }
}
