package com.example.jobportal;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Full application context test — requires a running PostgreSQL, Redis, and other
 * infrastructure services. Disabled in unit-test runs; enable manually when the
 * full stack is available (e.g., via docker-compose).
 */
@Disabled("Requires full infrastructure (PostgreSQL, Redis, etc.) — run manually with docker-compose")
@SpringBootTest
class JobPortalApplicationTests {

    @Test
    void contextLoads() {
    }

}
