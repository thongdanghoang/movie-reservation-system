package com.atomicbunker.reservation.resource;

import com.atomicbunker.reservation.dto.TicketResponse;
import com.atomicbunker.reservation.service.TicketService;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Path("/api/v1/reservations")
@Produces(MediaType.APPLICATION_JSON)
@RequiredArgsConstructor
public class TicketResource {

    private final TicketService ticketService;

    @GET
    @Path("/{reservationId}/ticket")
    public Uni<TicketResponse> getTicket(@PathParam("reservationId") UUID reservationId) {
        return ticketService.getTicket(reservationId);
    }
}
