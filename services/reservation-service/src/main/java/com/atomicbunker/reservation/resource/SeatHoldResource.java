package com.atomicbunker.reservation.resource;

import com.atomicbunker.reservation.dto.HoldSeatRequest;
import com.atomicbunker.reservation.dto.HoldSeatResponse;
import com.atomicbunker.reservation.exception.SeatAlreadyTakenException;
import com.atomicbunker.reservation.service.SeatHoldService;
import io.smallrye.mutiny.Uni;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.UUID;

@Path("/api/v1/seats")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequiredArgsConstructor
public class SeatHoldResource {

    private final SeatHoldService seatHoldService;

    @POST
    @Path("/{seatId}/hold")
    public Uni<Response> holdSeat(
            @PathParam("seatId") UUID seatId,
            @Valid HoldSeatRequest request) {

        return seatHoldService.holdSeat(seatId, request.sessionId())
                .map(response -> Response.ok(response).build())
                .onFailure(SeatAlreadyTakenException.class)
                .recoverWithItem(ex -> Response.status(Response.Status.CONFLICT)
                        .entity(Map.of(
                                "code", "SEAT_TAKEN",
                                "message", ex.getMessage()))
                        .build());
    }

    @DELETE
    @Path("/{seatId}/hold")
    public Uni<Response> releaseSeatHold(
            @PathParam("seatId") UUID seatId,
            @HeaderParam("X-Session-ID") String sessionId) {
        return seatHoldService.releaseSeatHold(seatId, sessionId)
                .map(v -> Response.noContent().build())
                .onFailure(jakarta.ws.rs.NotFoundException.class)
                .recoverWithItem(ex -> Response.status(Response.Status.NOT_FOUND)
                        .entity(Map.of(
                                "code", "NOT_FOUND",
                                "message", ex.getMessage()))
                        .build())
                .onFailure(jakarta.ws.rs.ForbiddenException.class)
                .recoverWithItem(ex -> Response.status(Response.Status.FORBIDDEN)
                        .entity(Map.of(
                                "code", "FORBIDDEN",
                                "message", ex.getMessage()))
                        .build());
    }
}
