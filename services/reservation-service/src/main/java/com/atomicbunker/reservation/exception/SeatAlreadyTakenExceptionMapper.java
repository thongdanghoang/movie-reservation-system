package com.atomicbunker.reservation.exception;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.Map;
import java.util.UUID;

@Provider
public class SeatAlreadyTakenExceptionMapper implements ExceptionMapper<SeatAlreadyTakenException> {
    
    @Override
    public Response toResponse(SeatAlreadyTakenException exception) {
        Map<String, String> error = Map.of(
            "code", "SEAT_TAKEN",
            "message", exception.getMessage(),
            "traceId", UUID.randomUUID().toString()
        );
        
        return Response.status(Response.Status.CONFLICT)
            .type(MediaType.APPLICATION_JSON)
            .entity(error)
            .build();
    }
}
