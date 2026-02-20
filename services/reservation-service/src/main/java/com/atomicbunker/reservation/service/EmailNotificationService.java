package com.atomicbunker.reservation.service;

import com.atomicbunker.reservation.domain.Seat;
import io.quarkus.vertx.ConsumeEvent;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;

@ApplicationScoped
@Slf4j
public class EmailNotificationService {

    @ConsumeEvent("seat-sold-email")
    public Uni<Void> sendEmailConfirmation(Seat seat) {
        log.info("Received SEAT_SOLD event for reservation [{}]. Preparing email...", seat.getReservationId());

        return Uni.createFrom().item(seat)
                .onItem().delayIt().by(Duration.ofSeconds(2))
                .onItem().invoke(s -> {
                    String ticketUrl = "http://localhost:3000/booking/confirmation/" + s.getReservationId();
                    log.info("=========================================================");
                    log.info("EMAIL SENT TO: {}", s.getEmail());
                    log.info("SUBJECT: Your Movie Ticket Confirmation");
                    log.info("BODY:");
                    log.info("Thank you for your purchase! Your seat {}{} is confirmed.", s.getSeatRow(),
                            s.getSeatColumn());
                    log.info("You can view and present your ticket here: {}", ticketUrl);
                    log.info("=========================================================");
                })
                .replaceWithVoid();
    }
}
