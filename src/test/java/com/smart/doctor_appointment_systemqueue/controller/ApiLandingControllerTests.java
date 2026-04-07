package com.smart.doctor_appointment_systemqueue.controller;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class ApiLandingControllerTests {
    @Test
    void rootUrlReturnsHelpfulApiPayload() {
        ApiLandingController controller = new ApiLandingController("");
        ResponseEntity<?> response = controller.getRootLanding();

        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().toString().contains("running"));
        assertTrue(response.getBody().toString().contains("/api/doctors"));
    }

    @Test
    void apiRootReturnsHelpfulApiPayload() {
        ApiLandingController controller = new ApiLandingController("");
        ResponseEntity<java.util.Map<String, Object>> response = controller.getApiLanding();

        assertEquals("Smart Queue Doctor Appointment API", response.getBody().get("name"));
        assertTrue(response.getBody().toString().contains("/api/appointments"));
    }

    @Test
    void rootRedirectsToFrontendWhenConfigured() {
        ApiLandingController controller = new ApiLandingController("https://smart-queue-free-doctor-appointment.vercel.app");
        ResponseEntity<?> response = controller.getRootLanding();

        assertEquals(HttpStatus.FOUND, response.getStatusCode());
        assertEquals("https://smart-queue-free-doctor-appointment.vercel.app", response.getHeaders().getLocation().toString());
    }
}
