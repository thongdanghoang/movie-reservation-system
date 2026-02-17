package com.atomicbunker.reservation.domain;

import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class ShowtimeRepository implements PanacheRepositoryBase<Showtime, UUID> {

    public Uni<List<Showtime>> findByMovieAndDateRange(UUID movieId, Instant startOfDay, Instant endOfDay) {
        return list("movie.id = ?1 and startTime >= ?2 and startTime < ?3", movieId, startOfDay, endOfDay);
    }
}
