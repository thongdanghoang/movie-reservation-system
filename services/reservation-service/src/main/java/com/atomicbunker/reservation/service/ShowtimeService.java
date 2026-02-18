package com.atomicbunker.reservation.service;

import com.atomicbunker.reservation.domain.Showtime;
import com.atomicbunker.reservation.domain.ShowtimeRepository;
import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
@RequiredArgsConstructor
public class ShowtimeService {

    private final ShowtimeRepository showtimeRepository;

    @WithSession
    public Uni<List<Showtime>> getShowtimesByMovieAndDate(UUID movieId, LocalDate date) {
        var startOfDay = date.atStartOfDay().toInstant(ZoneOffset.UTC);
        var endOfDay = date.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);
        var isToday = date.equals(LocalDate.now(ZoneOffset.UTC));

        if (isToday) {
            var now = Instant.now();
            return showtimeRepository.findByMovieAndDateRangeExcludingPast(movieId, startOfDay, endOfDay, now);
        } else {
            return showtimeRepository.findByMovieAndDateRange(movieId, startOfDay, endOfDay);
        }
    }

    @WithSession
    public Uni<Showtime> findById(UUID showtimeId) {
        return showtimeRepository.findById(showtimeId);
    }
}
