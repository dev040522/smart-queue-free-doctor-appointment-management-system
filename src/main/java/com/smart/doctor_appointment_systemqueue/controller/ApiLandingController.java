package com.smart.doctor_appointment_systemqueue.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ApiLandingController {
    @GetMapping({"/", "/api"})
    public ResponseEntity<Map<String, Object>> getApiLanding() {
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

        return ResponseEntity.ok(payload);
    }
}
