package com.atomicbunker.reservation.exception;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.Map;
import java.util.UUID;

@Provider
public class HoldExpiredExceptionMapper implements ExceptionMapper<HoldExpiredException> {

    @Override
    public Response toResponse(HoldExpiredException exception) {
        Map<String, String> error = Map.of(
                "code", "HOLD_EXPIRED",
                "message", exception.getMessage(),
                "traceId", UUID.randomUUID().toString());

        return Response.status(Response.Status.GONE)
                .type(MediaType.APPLICATION_JSON)
                .entity(error)
                .build();
    }
}
