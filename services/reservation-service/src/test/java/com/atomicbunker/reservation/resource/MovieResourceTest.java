package com.atomicbunker.reservation.resource;

import io.quarkus.test.TestReactiveTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.vertx.UniAsserter;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.not;

@QuarkusTest
public class MovieResourceTest {

    @Test
    @TestReactiveTransaction
    public void testGetNowPlayingMovies(UniAsserter asserter) {
        asserter.execute(() -> given()
                .when().get("/api/v1/movies/now-playing")
                .then()
                .statusCode(200)
                .body("size()", not(0)));
    }
}
