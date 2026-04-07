package com.smart.doctor_appointment_systemqueue.controller;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ApiLandingController {
    private final String frontendUrl;

    public ApiLandingController(@Value("${app.frontend.url:}") String frontendUrl) {
        this.frontendUrl = frontendUrl == null ? "" : frontendUrl.trim();
    }

    @GetMapping("/")
    public ResponseEntity<?> getRootLanding() {
        if (!frontendUrl.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create(frontendUrl))
                    .build();
        }

        return ResponseEntity.ok(buildApiPayload());
    }

    @GetMapping("/api")
    public ResponseEntity<Map<String, Object>> getApiLanding() {
        return ResponseEntity.ok(buildApiPayload());
    }

    private Map<String, Object> buildApiPayload() {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("name", "Smart Queue Doctor Appointment API");
        payload.put("status", "running");
        payload.put("message", "Use the /api routes listed below.");
        payload.put("endpoints", Map.of(
                "doctors", "/api/doctors",
                "users", "/api/users",
                "appointments", "/api/appointments",
                "admin", "/api/admin",
                "h2Console", "/api/h2-console"
        ));
        return payload;
    }
}
