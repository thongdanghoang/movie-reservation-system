package com.atomicbunker.reservation.domain;

import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class MovieRepository implements PanacheRepositoryBase<Movie, UUID> {

    public Uni<List<Movie>> findNowPlaying() {
        return list("status", Movie.Status.NOW_PLAYING);
    }
}
