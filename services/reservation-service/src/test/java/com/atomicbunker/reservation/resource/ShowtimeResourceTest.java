package com.atomicbunker.reservation.resource;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;

@QuarkusTest
public class ShowtimeResourceTest {

    @Test
    public void testGetShowtimesWithInvalidUuid() {
        given()
                .when().get("/api/v1/movies/invalid-uuid/showtimes")
                .then()
                .statusCode(400)
                .body("error", containsString("Invalid movie ID format"));
    }

    @Test
    public void testGetShowtimesWithInvalidDateFormat() {
        given()
                .when().get("/api/v1/movies/550e8400-e29b-41d4-a716-446655440000/showtimes?date=invalid-date")
                .then()
                .statusCode(400)
                .body("error", containsString("Invalid date format"));
    }
}
