package com.atomicbunker.reservation.domain;

import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class SeatRepository implements PanacheRepositoryBase<Seat, UUID> {

    public Uni<List<Seat>> findByShowtimeId(UUID showtimeId) {
        return list("showtime.id", showtimeId);
    }

    public Uni<Long> countByShowtimeIdAndStatus(UUID showtimeId, SeatStatus status) {
        return count("showtime.id = ?1 and status = ?2", showtimeId, status);
    }
}
