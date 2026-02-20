package com.atomicbunker.reservation.service;

import com.atomicbunker.reservation.domain.Seat;
import io.quarkus.vertx.ConsumeEvent;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Duration;

@ApplicationScoped
@Slf4j
public class EmailNotificationService {

    @ConfigProperty(name = "app.frontend.base-url")
    String frontendBaseUrl;

    @ConsumeEvent("seat-sold-email")
    public Uni<Void> sendEmailConfirmation(Seat seat) {
        log.info("Received SEAT_SOLD event for reservation [{}]. Preparing email...", seat.getReservationId());

        return Uni.createFrom().item(seat)
                .onItem().delayIt().by(Duration.ofSeconds(2))
                .onItem().invoke(s -> {
                    String ticketUrl = frontendBaseUrl + "/booking/confirmation/" + s.getReservationId();

                    // Mask email for logging: user@example.com -> u***@e***.com
                    String maskedEmail = maskEmail(s.getEmail());

                    log.info("=========================================================");
                    log.info("EMAIL SENT TO: {}", maskedEmail);
                    log.info("SUBJECT: Your Movie Ticket Confirmation");
                    log.info("BODY:");
                    log.info("Thank you for your purchase! Your seat {}{} is confirmed.", s.getSeatRow(),
                            s.getSeatColumn());
                    log.info("You can view and present your ticket here: {}", ticketUrl);
                    log.info("=========================================================");
                })
                .replaceWithVoid();
    }

    private String maskEmail(String email) {
        if (email == null || !email.contains("@"))
            return "****";
        String[] parts = email.split("@");
        String name = parts[0];
        String domain = parts[1];

        String maskedName = name.charAt(0) + "***";
        String maskedDomain = domain.charAt(0) + "***." + domain.substring(domain.lastIndexOf(".") + 1);

        return maskedName + "@" + maskedDomain;
    }
}
