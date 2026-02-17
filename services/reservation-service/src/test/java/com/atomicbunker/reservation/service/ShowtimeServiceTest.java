package com.atomicbunker.reservation.service;

import io.quarkus.test.TestReactiveTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.vertx.UniAsserter;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@QuarkusTest
public class ShowtimeServiceTest {

    @Inject
    ShowtimeService showtimeService;

    @Test
    @TestReactiveTransaction
    public void testGetShowtimesByMovieAndDate(UniAsserter asserter) {
        UUID randomMovieId = UUID.randomUUID();
        LocalDate testDate = LocalDate.of(2026, 2, 17);

        asserter.assertThat(() -> showtimeService.getShowtimesByMovieAndDate(randomMovieId, testDate),
                showtimes -> assertNotNull(showtimes));
    }

    @Test
    @TestReactiveTransaction
    public void testGetShowtimesForToday(UniAsserter asserter) {
        UUID randomMovieId = UUID.randomUUID();
        LocalDate today = LocalDate.now();

        asserter.assertThat(() -> showtimeService.getShowtimesByMovieAndDate(randomMovieId, today),
                showtimes -> assertNotNull(showtimes));
    }
}
