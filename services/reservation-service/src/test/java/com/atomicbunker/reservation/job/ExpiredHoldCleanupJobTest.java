package com.atomicbunker.reservation.job;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class ExpiredHoldCleanupJobTest {

    @Test
    void testJobExists() {
        ExpiredHoldCleanupJob job = new ExpiredHoldCleanupJob();
        assertNotNull(job);
    }
}