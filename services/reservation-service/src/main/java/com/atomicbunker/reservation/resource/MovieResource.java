package com.atomicbunker.reservation.resource;

import com.atomicbunker.reservation.service.MovieService;
import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.UUID;

@Path("/api/v1/movies")
@Produces(MediaType.APPLICATION_JSON)
public class MovieResource {

    @Inject
    MovieService movieService;

    @GET
    @Path("/now-playing")
    @WithSession
    public Uni<Response> getNowPlaying() {
        return movieService.getNowPlaying()
                .map(movies -> Response.ok(movies).build());
    }

    @GET
    @Path("/{id}")
    @WithSession
    public Uni<Response> getMovie(@PathParam("id") UUID id) {
        return movieService.getMovie(id)
                .map(movie -> {
                    if (movie == null) {
                        return Response.status(Response.Status.NOT_FOUND).build();
                    }
                    return Response.ok(movie).build();
                });
    }
}
