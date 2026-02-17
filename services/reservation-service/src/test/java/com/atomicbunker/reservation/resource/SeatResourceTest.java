package com.atomicbunker.reservation.resource;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;

@QuarkusTest
public class SeatResourceTest {

    @Test
    public void testGetSeatsWithInvalidUuid() {
        given()
                .when().get("/api/v1/showtimes/invalid-uuid/seats")
                .then()
                .statusCode(400)
                .body("error", containsString("Invalid showtime ID format"));
    }

    @Test
    public void testGetSeatsForNonExistentShowtime() {
        given()
                .when().get("/api/v1/showtimes/550e8400-e29b-41d4-a716-446655440000/seats")
                .then()
                .statusCode(404)
                .body("error", containsString("Showtime not found"));
    }
}
