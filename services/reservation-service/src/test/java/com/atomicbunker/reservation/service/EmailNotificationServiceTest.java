package com.atomicbunker.reservation.service;

import com.atomicbunker.reservation.domain.Seat;
import com.atomicbunker.reservation.domain.SeatStatus;
import io.quarkus.test.junit.QuarkusTest;
import io.vertx.core.eventbus.EventBus;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import io.quarkus.test.junit.mockito.InjectSpy;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
public class EmailNotificationServiceTest {

    @Inject
    EventBus eventBus;

    @InjectSpy
    EmailNotificationService emailNotificationService;

    @Test
    public void testSeatSoldEmailBroadcastsWithoutError() throws InterruptedException {
        Seat seat = new Seat();
        seat.setId(UUID.randomUUID());
        seat.setReservationId(UUID.randomUUID());
        seat.setShowtimeId(UUID.randomUUID());
        seat.setEmail("test-email@example.com");
        seat.setSeatRow("A");
        seat.setSeatColumn(1);
        seat.setStatus(SeatStatus.SOLD);
        seat.setPaidAt(Instant.now());

        CountDownLatch latch = new CountDownLatch(1);

        // When the service method is called, count down the latch
        Mockito.doAnswer(invocation -> {
            latch.countDown();
            return invocation.callRealMethod();
        }).when(emailNotificationService).sendEmailConfirmation(Mockito.any());

        // Target the event
        eventBus.publish("seat-sold-email", seat);

        // Wait for the consumer to be invoked
        boolean completed = latch.await(5, TimeUnit.SECONDS);
        assertTrue(completed, "EmailNotificationService consumer was not invoked within timeout");
    }
}
