package com.atomicbunker.reservation.resource;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;

@QuarkusTest
public class ShowtimeResourceTest {

    @Test
    public void testGetShowtimesWithInvalidUuid() {
        // With native UUID type, invalid UUID format results in 404 (route doesn't match)
        given()
                .when().get("/api/v1/movies/invalid-uuid/showtimes")
                .then()
                .statusCode(404);
    }

    @Test
    public void testGetShowtimesWithInvalidDateFormat() {
        // With native LocalDate type, invalid date format results in 404 (route doesn't match)
        given()
                .when().get("/api/v1/movies/550e8400-e29b-41d4-a716-446655440000/showtimes?date=invalid-date")
                .then()
                .statusCode(404);
    }
}
