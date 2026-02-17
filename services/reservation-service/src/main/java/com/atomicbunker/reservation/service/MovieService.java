package com.atomicbunker.reservation.service;

import com.atomicbunker.reservation.domain.Movie;
import com.atomicbunker.reservation.domain.MovieRepository;
import com.atomicbunker.reservation.exception.NotFoundException;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class MovieService {

    @Inject
    MovieRepository movieRepository;

    public Uni<List<Movie>> getNowPlaying() {
        return movieRepository.findNowPlaying();
    }

    public Uni<Movie> getMovie(UUID id) {
        return movieRepository.findById(id)
                .onItem().ifNull().failWith(() -> new NotFoundException("Movie not found with id: " + id));
    }
}
