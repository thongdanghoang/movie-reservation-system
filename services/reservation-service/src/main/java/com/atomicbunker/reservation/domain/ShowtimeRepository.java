package com.atomicbunker.reservation.domain;

import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Parameters;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class ShowtimeRepository implements PanacheRepositoryBase<Showtime, UUID> {

    public Uni<List<Showtime>> findByMovieAndDateRange(UUID movieId, Instant startOfDay, Instant endOfDay) {
        return list("""
                select s
                from Showtime s
                left join fetch s.movie
                where s.movie.id = :movieId
                  and s.startTime >= :startOfDay
                  and s.startTime < :endOfDay
                """,
                Parameters.with("movieId", movieId)
                        .and("startOfDay", startOfDay)
                        .and("endOfDay", endOfDay)
        );
    }

    public Uni<List<Showtime>> findByMovieAndDateRangeExcludingPast(UUID movieId, Instant startOfDay, Instant endOfDay, Instant now) {
        return list("""
                select s
                from Showtime s
                left join fetch s.movie
                where s.movie.id = :movieId
                  and s.startTime >= :startOfDay
                  and s.startTime < :endOfDay
                  and s.startTime >= :now
                """,
                Parameters.with("movieId", movieId)
                        .and("startOfDay", startOfDay)
                        .and("endOfDay", endOfDay)
                        .and("now", now)
        );
    }
}
