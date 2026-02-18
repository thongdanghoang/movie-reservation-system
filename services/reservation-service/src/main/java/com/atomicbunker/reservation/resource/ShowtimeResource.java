package com.atomicbunker.reservation.resource;

import com.atomicbunker.reservation.dto.ShowtimeDTO;
import com.atomicbunker.reservation.mapper.ShowtimeMapper;
import com.atomicbunker.reservation.service.ShowtimeService;
import io.smallrye.mutiny.Uni;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Path("/api/v1/movies/{movieId}/showtimes")
@Produces(MediaType.APPLICATION_JSON)
@RequiredArgsConstructor
public class ShowtimeResource {

    private final ShowtimeService showtimeService;
    private final ShowtimeMapper showtimeMapper;

    @GET
    public Uni<List<ShowtimeDTO>> getShowtimes(
            @PathParam("movieId") @NotNull UUID movieId,
            @QueryParam("date") LocalDate date) {

        LocalDate queryDate = date != null ? date : LocalDate.now(ZoneOffset.UTC);

        return showtimeService.getShowtimesByMovieAndDate(movieId, queryDate)
                .map(showtimeMapper::toDTOList);
    }
}
