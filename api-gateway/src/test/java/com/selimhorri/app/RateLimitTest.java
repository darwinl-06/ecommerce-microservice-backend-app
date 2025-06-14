package com.selimhorri.app;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RateLimitTest {

    private static final Logger log = LoggerFactory.getLogger(RateLimitTest.class);

    @Autowired
    private TestRestTemplate restTemplate;

    @BeforeEach
    void setUp() {
        // Wait for Redis to be fully initialized
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    @Test
    void testRateLimitConfiguration() {
        ResponseEntity<String> response = restTemplate.getForEntity("/actuator/gateway/routes", String.class);

        assertTrue(response.getStatusCode().is2xxSuccessful(),
                "Should be able to get gateway routes");
        assertTrue(response.getBody().contains("RequestRateLimiter"),
                "Rate limiting should be configured");
    }

    @Test
    void testRateLimit() {
        String testEndpoint = "/product-service/test";
        int requests = 5;

        for (int i = 0; i < requests; i++) {
            ResponseEntity<String> response = restTemplate.getForEntity(testEndpoint, String.class);
            log.info("Request {} status: {}", i + 1, response.getStatusCode());
        }
    }
}