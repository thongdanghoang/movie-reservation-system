package com.atomicbunker.reservation.resource;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;

@QuarkusTest
public class TicketResourceTest {

        private String getAvailableSeatId() {
                // "Inception" showtime ID from R__test_data.sql
                String showtimeId = "660e8400-e29b-41d4-a716-446655440001";

                return given()
                                .when()
                                .get("/api/v1/showtimes/{showtimeId}/seats", showtimeId)
                                .then()
                                .statusCode(200)
                                .extract()
                                .jsonPath()
                                .getString("find { it.status == 'AVAILABLE' }.id");
        }

        @Test
        public void testGetTicketReturns200ForSoldSeat() {
                String seatId = getAvailableSeatId();

                String holdResponse = given()
                                .contentType(ContentType.JSON)
                                .body("{\"sessionId\":\"test-session-" + UUID.randomUUID() + "\"}")
                                .when()
                                .post("/api/v1/seats/{seatId}/hold", seatId)
                                .then()
                                .statusCode(200)
                                .extract()
                                .asString();

                io.restassured.path.json.JsonPath holdJson = io.restassured.path.json.JsonPath.from(holdResponse);
                String reservationId = holdJson.getString("reservationId");
                String sessionId = holdJson.getString("sessionId");

                // Step 2: Pay for the held seat
                given()
                                .contentType(ContentType.JSON)
                                .header("X-Session-ID", sessionId)
                                .body("{\"email\":\"ticketguest@example.com\",\"phone\":\"555-0888\"}")
                                .when()
                                .post("/api/v1/reservations/{reservationId}/pay", reservationId)
                                .then()
                                .statusCode(200);

                // Step 3: Fetch the ticket with CORRECT session ID
                given()
                                .header("X-Session-ID", sessionId)
                                .when()
                                .get("/api/v1/reservations/{reservationId}/ticket", reservationId)
                                .then()
                                .statusCode(200)
                                .body("reservationId", equalTo(reservationId))
                                .body("status", equalTo("SOLD"))
                                .body("seatId", equalTo(seatId))
                                .body("email", equalTo("ticketguest@example.com"));
        }

        @Test
        public void testGetTicketReturns403ForWrongSession() {
                String seatId = getAvailableSeatId();

                String holdResponse = given()
                                .contentType(ContentType.JSON)
                                .body("{\"sessionId\":\"owner-session\"}")
                                .when()
                                .post("/api/v1/seats/{seatId}/hold", seatId)
                                .then()
                                .statusCode(200)
                                .extract()
                                .asString();

                String reservationId = io.restassured.path.json.JsonPath.from(holdResponse).getString("reservationId");

                // Pay
                given()
                                .contentType(ContentType.JSON)
                                .header("X-Session-ID", "owner-session")
                                .body("{\"email\":\"owner@example.com\",\"phone\":\"555-0000\"}")
                                .when()
                                .post("/api/v1/reservations/{reservationId}/pay", reservationId)
                                .then()
                                .statusCode(200);

                // Fetch with WRONG session ID
                given()
                                .header("X-Session-ID", "attacker-session")
                                .when()
                                .get("/api/v1/reservations/{reservationId}/ticket", reservationId)
                                .then()
                                .statusCode(403);
        }

        @Test
        public void testGetTicketReturns404ForNonExistentReservation() {
                UUID randomId = UUID.randomUUID();
                given()
                                .header("X-Session-ID", "some-session")
                                .when()
                                .get("/api/v1/reservations/{reservationId}/ticket", randomId)
                                .then()
                                .statusCode(404);
        }

        @Test
        public void testGetTicketReturns404ForHeldButNotSoldSeat() {
                String seatId = getAvailableSeatId();

                String holdResponse = given()
                                .contentType(ContentType.JSON)
                                .body("{\"sessionId\":\"some-session\"}")
                                .when()
                                .post("/api/v1/seats/{seatId}/hold", seatId)
                                .then()
                                .statusCode(200)
                                .extract()
                                .asString();

                String reservationId = io.restassured.path.json.JsonPath.from(holdResponse).getString("reservationId");

                // Try getting ticket (held but not SOLD)
                given()
                                .header("X-Session-ID", "some-session")
                                .when()
                                .get("/api/v1/reservations/{reservationId}/ticket", reservationId)
                                .then()
                                .statusCode(404);
        }
}
