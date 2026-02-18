package com.atomicbunker.reservation.service;

import com.atomicbunker.reservation.domain.Movie;
import com.atomicbunker.reservation.domain.MovieRepository;
import com.atomicbunker.reservation.exception.NotFoundException;
import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
@RequiredArgsConstructor
public class MovieService {

    private final MovieRepository movieRepository;

    @WithSession
    public Uni<List<Movie>> getNowPlaying() {
        return movieRepository.findNowPlaying();
    }

    @WithSession
    public Uni<Movie> getMovie(UUID id) {
        return movieRepository.findById(id)
                .onItem().ifNull().failWith(() -> new NotFoundException("Movie not found with id: " + id));
    }
}
