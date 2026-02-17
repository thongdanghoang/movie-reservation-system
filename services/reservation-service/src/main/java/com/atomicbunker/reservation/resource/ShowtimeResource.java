package com.atomicbunker.reservation.resource;

import com.atomicbunker.reservation.service.ShowtimeService;
import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.UUID;

@Path("/api/v1/movies/{movieId}/showtimes")
@Produces(MediaType.APPLICATION_JSON)
public class ShowtimeResource {

    @Inject
    ShowtimeService showtimeService;

    @GET
    @WithSession
    public Uni<Response> getShowtimes(
            @PathParam("movieId") String movieIdStr,
            @QueryParam("date") String dateStr) {

        UUID movieId;
        try {
            movieId = UUID.fromString(movieIdStr);
        } catch (IllegalArgumentException e) {
            return Uni.createFrom().item(
                    Response.status(Response.Status.BAD_REQUEST)
                            .entity("{\"error\":\"Invalid movie ID format. Must be a valid UUID.\"}")
                            .build());
        }

        LocalDate date;
        if (dateStr == null || dateStr.isBlank()) {
            date = LocalDate.now();
        } else {
            try {
                date = LocalDate.parse(dateStr);
            } catch (DateTimeException e) {
                return Uni.createFrom().item(
                        Response.status(Response.Status.BAD_REQUEST)
                                .entity("{\"error\":\"Invalid date format. Use ISO-8601 format (YYYY-MM-DD).\"}")
                                .build());
            }
        }

        return showtimeService.getShowtimesByMovieAndDate(movieId, date)
                .map(showtimes -> Response.ok(showtimes).build());
    }
}
