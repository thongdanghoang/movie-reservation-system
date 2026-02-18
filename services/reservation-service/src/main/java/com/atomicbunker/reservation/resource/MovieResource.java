package com.atomicbunker.reservation.resource;

import com.atomicbunker.reservation.domain.Movie;
import com.atomicbunker.reservation.dto.MovieDTO;
import com.atomicbunker.reservation.service.MovieService;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import java.util.UUID;

@Path("/api/v1/movies")
@Produces(MediaType.APPLICATION_JSON)
@RequiredArgsConstructor
public class MovieResource {

    private final MovieService movieService;

    @GET
    @Path("/now-playing")
    public Uni<MovieDTO[]> getNowPlaying() {
        return movieService.getNowPlaying()
                .map(movies -> movies.stream()
                        .map(this::toDTO)
                        .toArray(MovieDTO[]::new));
    }

    @GET
    @Path("/{id}")
    public Uni<MovieDTO> getMovie(@PathParam("id") UUID id) {
        return movieService.getMovie(id)
                .map(this::toDTO);
    }

    private MovieDTO toDTO(Movie movie) {
        return new MovieDTO(
                movie.getId(),
                movie.getTitle(),
                movie.getPosterUrl(),
                movie.getGenre(),
                movie.getStatus() != null ? movie.getStatus().name() : null
        );
    }
}
