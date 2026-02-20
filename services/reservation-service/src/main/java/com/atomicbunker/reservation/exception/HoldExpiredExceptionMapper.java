package com.atomicbunker.reservation.exception;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Provider
public class HoldExpiredExceptionMapper implements ExceptionMapper<HoldExpiredException> {

    @Override
    public Response toResponse(HoldExpiredException exception) {
        // Use LinkedHashMap to avoid Map.of() NPE when getMessage() is null
        String message = exception.getMessage() != null ? exception.getMessage() : "Hold has expired";
        Map<String, String> error = new LinkedHashMap<>();
        error.put("code", "HOLD_EXPIRED");
        error.put("message", message);
        error.put("traceId", UUID.randomUUID().toString());

        return Response.status(Response.Status.GONE)
                .type(MediaType.APPLICATION_JSON)
                .entity(error)
                .build();
    }
}
