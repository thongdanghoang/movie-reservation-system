package com.atomicbunker.reservation.resource;

import com.atomicbunker.reservation.dto.ShowtimeDTO;
import com.atomicbunker.reservation.exception.BadRequestException;
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
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Path("/api/v1/movies/{movieId}/showtimes")
@Produces(MediaType.APPLICATION_JSON)
public class ShowtimeResource {

    @Inject
    ShowtimeService showtimeService;

    @GET
    @WithSession
    public Uni<List<ShowtimeDTO>> getShowtimes(
            @PathParam("movieId") String movieIdStr,
            @QueryParam("date") String dateStr) {

        UUID movieId;
        try {
            movieId = UUID.fromString(movieIdStr);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid movie ID format. Must be a valid UUID.");
        }

        LocalDate date;
        if (dateStr == null || dateStr.isBlank()) {
            date = LocalDate.now(ZoneOffset.UTC);
        } else {
            try {
                date = LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE);
            } catch (DateTimeException e) {
                throw new BadRequestException("Invalid date format. Use ISO-8601 format (YYYY-MM-DD).");
            }
        }

        return showtimeService.getShowtimesByMovieAndDate(movieId, date);
    }
}
