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
        var startOfDay = date.atStartOfDay().toInstant(ZoneOffset.UTC);
        var endOfDay = date.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);
        var isToday = date.equals(LocalDate.now(ZoneOffset.UTC));

        Uni<List<Showtime>> showtimesUni;
        if (isToday) {
            var now = Instant.now();
            showtimesUni = showtimeRepository.findByMovieAndDateRangeExcludingPast(movieId, startOfDay, endOfDay, now);
        } else {
            showtimesUni = showtimeRepository.findByMovieAndDateRange(movieId, startOfDay, endOfDay);
        }

        return showtimesUni.map(showtimes -> showtimes.stream()
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
