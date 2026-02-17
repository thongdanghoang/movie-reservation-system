package com.atomicbunker.reservation.service;

import com.atomicbunker.reservation.domain.Showtime;
import com.atomicbunker.reservation.domain.ShowtimeRepository;
import com.atomicbunker.reservation.dto.ShowtimeDTO;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class ShowtimeService {

    @Inject
    ShowtimeRepository showtimeRepository;

    public Uni<List<ShowtimeDTO>> getShowtimesByMovieAndDate(UUID movieId, LocalDate date) {
        Instant startOfDay = date.atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant endOfDay = date.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);

        return showtimeRepository.findByMovieAndDateRange(movieId, startOfDay, endOfDay)
                .map(showtimes -> {
                    // Filter out past showtimes if querying for today
                    if (date.equals(LocalDate.now())) {
                        Instant now = Instant.now();
                        return showtimes.stream()
                                .filter(s -> !s.startTime.isBefore(now))
                                .toList();
                    }
                    return showtimes;
                })
                .map(showtimes -> showtimes.stream()
                        .map(s -> new ShowtimeDTO(
                                s.id,
                                s.movie != null ? s.movie.id : null,
                                s.movie != null ? s.movie.title : null,
                                s.startTime,
                                s.theaterName,
                                s.availableSeats
                        ))
                        .toList());
    }
}
