package com.atomicbunker.reservation.exception;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Provider
public class SeatAlreadyTakenExceptionMapper implements ExceptionMapper<SeatAlreadyTakenException> {

    @Override
    public Response toResponse(SeatAlreadyTakenException exception) {
        // Use LinkedHashMap to avoid Map.of() NPE when getMessage() is null
        String message = exception.getMessage() != null ? exception.getMessage() : "Seat is no longer available";
        Map<String, String> error = new LinkedHashMap<>();
        error.put("code", "SEAT_TAKEN");
        error.put("message", message);
        error.put("traceId", UUID.randomUUID().toString());

        return Response.status(Response.Status.CONFLICT)
                .type(MediaType.APPLICATION_JSON)
                .entity(error)
                .build();
    }
}
