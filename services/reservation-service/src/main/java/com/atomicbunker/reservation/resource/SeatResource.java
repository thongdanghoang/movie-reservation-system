package com.atomicbunker.reservation.resource;

import com.atomicbunker.reservation.dto.SeatDTO;
import com.atomicbunker.reservation.exception.NotFoundException;
import com.atomicbunker.reservation.mapper.SeatMapper;
import com.atomicbunker.reservation.service.SeatService;
import com.atomicbunker.reservation.service.ShowtimeService;
import io.smallrye.mutiny.Uni;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import java.util.List;
import java.util.UUID;

@Path("/api/v1/showtimes/{showtimeId}/seats")
@Produces(MediaType.APPLICATION_JSON)
@RequiredArgsConstructor
public class SeatResource {

    private final SeatService seatService;
    private final ShowtimeService showtimeService;
    private final SeatMapper seatMapper;

    @GET
    public Uni<List<SeatDTO>> getSeats(@PathParam("showtimeId") @NotNull UUID showtimeId) {
        return showtimeService.findById(showtimeId)
                .onItem().ifNull().switchTo(() -> 
                    Uni.createFrom().failure(new NotFoundException("Showtime not found with id: " + showtimeId))
                )
                .chain(showtime -> seatService.getSeatsByShowtime(showtimeId))
                .map(seatMapper::toDTOList);
    }
}
