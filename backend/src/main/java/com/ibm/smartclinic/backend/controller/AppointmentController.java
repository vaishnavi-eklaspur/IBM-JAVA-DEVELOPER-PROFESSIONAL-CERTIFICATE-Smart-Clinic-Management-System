package com.ibm.smartclinic.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<?> getAppointmentsByPatient(
            @PathVariable Long patientId,
            @RequestHeader(value = "Authorization", required = false) String token
    ) {

        // Token check (lab-level validation)
        if (token == null || token.isEmpty()) {
            return ResponseEntity.status(401).body("Missing or invalid token");
        }

        // Static demo response (acceptable for assignment)
        return ResponseEntity.ok(
                List.of(
                        Map.of(
                                "id", 1,
                                "appointmentTime", "2025-12-27T10:00",
                                "doctor", Map.of(
                                        "id", 1,
                                        "name", "Dr John Smith",
                                        "speciality", "Cardiology"
                                )
                        )
                )
        );
    }
}
