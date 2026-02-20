package com.atomicbunker.reservation.resource;

import com.atomicbunker.reservation.dto.PaymentRequest;
import com.atomicbunker.reservation.dto.PaymentResponse;
import com.atomicbunker.reservation.service.PaymentService;
import io.smallrye.mutiny.Uni;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Path("/api/v1/reservations")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequiredArgsConstructor
public class PaymentResource {

    private final PaymentService paymentService;

    @POST
    @Path("/{reservationId}/pay")
    public Uni<Response> processPayment(
            @PathParam("reservationId") UUID reservationId,
            @HeaderParam("X-Session-ID") String sessionId,
            @Valid PaymentRequest request) {

        return paymentService.processPayment(reservationId, sessionId, request.email(), request.phone())
                .map(response -> Response.ok(response).build());
    }
}
