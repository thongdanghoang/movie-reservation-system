package com.atomicbunker.reservation.resource;

import com.atomicbunker.reservation.dto.SeatDTO;
import com.atomicbunker.reservation.exception.BadRequestException;
import com.atomicbunker.reservation.exception.NotFoundException;
import com.atomicbunker.reservation.service.SeatService;
import com.atomicbunker.reservation.service.ShowtimeService;
import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import java.util.UUID;

@Path("/api/v1/showtimes/{showtimeId}/seats")
@Produces(MediaType.APPLICATION_JSON)
public class SeatResource {

    @Inject
    SeatService seatService;

    @Inject
    ShowtimeService showtimeService;

    @GET
    @WithSession
    public Uni<List<SeatDTO>> getSeats(@PathParam("showtimeId") String showtimeIdStr) {
        UUID showtimeId;
        try {
            showtimeId = UUID.fromString(showtimeIdStr);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid showtime ID format. Must be a valid UUID.");
        }

        return showtimeService.findById(showtimeId)
                .onItem().ifNull().switchTo(() -> 
                    Uni.createFrom().failure(new NotFoundException("Showtime not found with id: " + showtimeIdStr))
                )
                .chain(showtime -> seatService.getSeatsByShowtime(showtimeId));
    }
}
