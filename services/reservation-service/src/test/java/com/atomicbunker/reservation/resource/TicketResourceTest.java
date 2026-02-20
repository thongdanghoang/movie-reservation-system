package com.atomicbunker.reservation.resource;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;

@QuarkusTest
public class TicketResourceTest {

    @Test
    public void testGetTicketReturns200ForSoldSeat() {
        // Step 1: Hold a seat
        String seatId = "770e8400-e29b-41d4-a716-446655440003";

        String holdResponse = given()
                .contentType(ContentType.JSON)
                .body("{\"sessionId\":\"test-ticket-" + UUID.randomUUID() + "\"}")
                .when()
                .post("/api/v1/seats/{seatId}/hold", seatId)
                .then()
                .statusCode(200)
                .extract()
                .body()
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

        // Step 3: Fetch the ticket
        given()
                .when()
                .get("/api/v1/reservations/{reservationId}/ticket", reservationId)
                .then()
                .statusCode(200)
                .body("reservationId", equalTo(reservationId))
                .body("status", equalTo("SOLD"))
                .body("seatId", equalTo(seatId))
                .body("email", equalTo("ticketguest@example.com"))
                .body("signedTicketToken", notNullValue())
                .body("paidAt", notNullValue());
    }

    @Test
    public void testGetTicketReturns404ForNonExistentReservation() {
        UUID randomId = UUID.randomUUID();
        given()
                .when()
                .get("/api/v1/reservations/{reservationId}/ticket", randomId)
                .then()
                .statusCode(404);
    }

    @Test
    public void testGetTicketReturns404ForHeldButNotSoldSeat() {
        // Hold seat but do not pay
        String seatId = "770e8400-e29b-41d4-a716-446655440004";

        String holdResponse = given()
                .contentType(ContentType.JSON)
                .body("{\"sessionId\":\"test-ticket-held-" + UUID.randomUUID() + "\"}")
                .when()
                .post("/api/v1/seats/{seatId}/hold", seatId)
                .then()
                .statusCode(200)
                .extract()
                .body()
                .asString();

        io.restassured.path.json.JsonPath holdJson = io.restassured.path.json.JsonPath.from(holdResponse);
        String reservationId = holdJson.getString("reservationId");

        // Try getting the ticket
        given()
                .when()
                .get("/api/v1/reservations/{reservationId}/ticket", reservationId)
                .then()
                .statusCode(404);
    }
}
