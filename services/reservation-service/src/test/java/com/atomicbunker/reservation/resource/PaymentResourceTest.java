package com.atomicbunker.reservation.resource;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.matchesPattern;

@QuarkusTest
public class PaymentResourceTest {

        // From R__test_data.sql: seat 770e8400-e29b-41d4-a716-446655448011 is HELD with
        // no reservationId/heldBy, so
        // for integration tests we call the hold endpoint first (real flow), then pay.
        // We test the non-happy-path scenarios directly using known UUIDs.

        @Test
        public void testPaymentReturns404WhenReservationNotFound() {
                UUID nonExistentReservationId = UUID.randomUUID();

                given()
                                .contentType(ContentType.JSON)
                                .body("{\"email\":\"test@example.com\",\"phone\":\"555-0100\"}")
                                .when()
                                .post("/api/v1/reservations/{reservationId}/pay", nonExistentReservationId)
                                .then()
                                .statusCode(404);
        }

        @Test
        public void testPaymentReturns400WhenEmailIsMissing() {
                UUID anyReservationId = UUID.randomUUID();

                given()
                                .contentType(ContentType.JSON)
                                .body("{\"phone\":\"555-0100\"}")
                                .when()
                                .post("/api/v1/reservations/{reservationId}/pay", anyReservationId)
                                .then()
                                .statusCode(400);
        }

        @Test
        public void testPaymentReturns400WhenPhoneIsMissing() {
                UUID anyReservationId = UUID.randomUUID();

                given()
                                .contentType(ContentType.JSON)
                                .body("{\"email\":\"test@example.com\"}")
                                .when()
                                .post("/api/v1/reservations/{reservationId}/pay", anyReservationId)
                                .then()
                                .statusCode(400);
        }

        @Test
        public void testPaymentReturns400WhenBodyIsEmpty() {
                UUID anyReservationId = UUID.randomUUID();

                given()
                                .contentType(ContentType.JSON)
                                .body("{}")
                                .when()
                                .post("/api/v1/reservations/{reservationId}/pay", anyReservationId)
                                .then()
                                .statusCode(400);
        }

        @Test
        public void testPaymentReturns400WhenEmailIsBlank() {
                UUID anyReservationId = UUID.randomUUID();

                given()
                                .contentType(ContentType.JSON)
                                .body("{\"email\":\"\",\"phone\":\"555-0100\"}")
                                .when()
                                .post("/api/v1/reservations/{reservationId}/pay", anyReservationId)
                                .then()
                                .statusCode(400);
        }

        @Test
        public void testSuccessfulPaymentFlow() {
                // Step 1: Hold a seat to get a reservationId
                String seatId = "770e8400-e29b-41d4-a716-446655440001"; // Row A, Seat 1 in Inception showtime 1

                String holdResponse = given()
                                .contentType(ContentType.JSON)
                                .body("{\"sessionId\":\"test-session-payment-" + UUID.randomUUID() + "\"}")
                                .when()
                                .post("/api/v1/seats/{seatId}/hold", seatId)
                                .then()
                                .statusCode(200)
                                .extract()
                                .body()
                                .asString();

                // Extract reservationId from the hold response
                String reservationId = io.restassured.path.json.JsonPath.from(holdResponse).getString("reservationId");

                // Step 2: Pay for the held seat
                given()
                                .contentType(ContentType.JSON)
                                .body("{\"email\":\"guest@example.com\",\"phone\":\"555-0123\"}")
                                .when()
                                .post("/api/v1/reservations/{reservationId}/pay", reservationId)
                                .then()
                                .statusCode(200)
                                .body("reservationId", equalTo(reservationId))
                                .body("status", equalTo("SOLD"))
                                .body("seatId", equalTo(seatId))
                                .body("confirmationNumber", matchesPattern("CNF-[A-Z0-9]{8}"));
        }

        @Test
        public void testPaymentReturns409WhenSeatAlreadySold() {
                // Step 1: Hold a fresh seat
                String seatId = "770e8400-e29b-41d4-a716-446655440002";

                String holdResponse = given()
                                .contentType(ContentType.JSON)
                                .body("{\"sessionId\":\"test-session-conflict-" + UUID.randomUUID() + "\"}")
                                .when()
                                .post("/api/v1/seats/{seatId}/hold", seatId)
                                .then()
                                .statusCode(200)
                                .extract().body().asString();

                String reservationId = io.restassured.path.json.JsonPath.from(holdResponse).getString("reservationId");

                // Step 2: First payment — succeeds
                given()
                                .contentType(ContentType.JSON)
                                .body("{\"email\":\"first@example.com\",\"phone\":\"555-0001\"}")
                                .when()
                                .post("/api/v1/reservations/{reservationId}/pay", reservationId)
                                .then()
                                .statusCode(200);

                // Step 3: Second payment — should fail with 409
                given()
                                .contentType(ContentType.JSON)
                                .body("{\"email\":\"second@example.com\",\"phone\":\"555-0002\"}")
                                .when()
                                .post("/api/v1/reservations/{reservationId}/pay", reservationId)
                                .then()
                                .statusCode(409)
                                .body("code", equalTo("SEAT_TAKEN"));
        }
}
