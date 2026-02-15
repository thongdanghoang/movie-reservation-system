package com.atomicbunker.reservation.service;

import com.atomicbunker.reservation.domain.Movie;
import com.atomicbunker.reservation.domain.MovieRepository;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;

@ApplicationScoped
public class MovieService {

    @Inject
    MovieRepository movieRepository;

    public Uni<List<Movie>> getNowPlaying() {
        return movieRepository.findNowPlaying();
    }
}
