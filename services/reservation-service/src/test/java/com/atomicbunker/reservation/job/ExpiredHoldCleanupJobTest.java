package com.atomicbunker.reservation.job;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class ExpiredHoldCleanupJobTest {

    @Inject
    ExpiredHoldCleanupJob job;

    @Test
    void testJobBeanExists() {
        assertNotNull(job);
    }
}
