package com.atomicbunker.reservation.service;

import io.quarkus.test.TestReactiveTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.vertx.UniAsserter;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
public class SeatServiceTest {

    @Inject
    SeatService seatService;

    @Test
    @TestReactiveTransaction
    public void testGetSeatsByShowtime(UniAsserter asserter) {
        UUID showtimeId = UUID.randomUUID();
        
        asserter.assertThat(() -> seatService.getSeatsByShowtime(showtimeId),
                seats -> {
                    assertNotNull(seats);
                    assertTrue(seats.isEmpty());
                });
    }

    @Test
    @TestReactiveTransaction
    public void testFindById(UniAsserter asserter) {
        UUID seatId = UUID.randomUUID();
        
        asserter.assertThat(() -> seatService.findById(seatId),
                seat -> assertNull(seat));
    }
}
