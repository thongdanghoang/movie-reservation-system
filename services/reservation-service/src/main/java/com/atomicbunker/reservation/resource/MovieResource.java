package com.atomicbunker.reservation.resource;

import com.atomicbunker.reservation.dto.MovieDTO;
import com.atomicbunker.reservation.service.MovieService;
import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.UUID;

@Path("/api/v1/movies")
@Produces(MediaType.APPLICATION_JSON)
public class MovieResource {

    @Inject
    MovieService movieService;

    @GET
    @Path("/now-playing")
    @WithSession
    public Uni<MovieDTO[]> getNowPlaying() {
        return movieService.getNowPlaying()
                .map(movies -> movies.stream()
                        .map(MovieDTO::from)
                        .toArray(MovieDTO[]::new));
    }

    @GET
    @Path("/{id}")
    @WithSession
    public Uni<MovieDTO> getMovie(@PathParam("id") UUID id) {
        return movieService.getMovie(id)
                .map(MovieDTO::from);
    }
}
