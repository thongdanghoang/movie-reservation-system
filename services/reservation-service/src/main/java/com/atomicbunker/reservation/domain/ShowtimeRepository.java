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
        return list("select s from Showtime s left join fetch s.movie where s.movie.id = ?1 and s.startTime >= ?2 and s.startTime < ?3", 
                    movieId, startOfDay, endOfDay);
    }

    public Uni<List<Showtime>> findByMovieAndDateRangeExcludingPast(UUID movieId, Instant startOfDay, Instant endOfDay, Instant now) {
        return list("select s from Showtime s left join fetch s.movie where s.movie.id = ?1 and s.startTime >= ?2 and s.startTime < ?3 and s.startTime >= ?4", 
                    movieId, startOfDay, endOfDay, now);
    }
}
