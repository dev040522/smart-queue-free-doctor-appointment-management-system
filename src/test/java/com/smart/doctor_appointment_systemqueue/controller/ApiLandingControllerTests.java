package com.smart.doctor_appointment_systemqueue.controller;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

class ApiLandingControllerTests {
    private final ApiLandingController controller = new ApiLandingController();

    @Test
    void rootUrlReturnsHelpfulApiPayload() {
        ResponseEntity<java.util.Map<String, Object>> response = controller.getApiLanding();

        assertEquals(200, response.getStatusCode().value());
        assertEquals("running", response.getBody().get("status"));
        assertTrue(response.getBody().toString().contains("/api/doctors"));
    }

    @Test
    void apiRootReturnsHelpfulApiPayload() {
        ResponseEntity<java.util.Map<String, Object>> response = controller.getApiLanding();

        assertEquals("Smart Queue Doctor Appointment API", response.getBody().get("name"));
        assertTrue(response.getBody().toString().contains("/api/appointments"));
    }
}
