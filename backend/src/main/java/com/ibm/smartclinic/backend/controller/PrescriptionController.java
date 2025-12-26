package com.ibm.smartclinic.backend.controller;

import com.ibm.smartclinic.backend.model.Prescription;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/prescriptions")
public class PrescriptionController {

    @PostMapping
    public ResponseEntity<?> savePrescription(
            @RequestHeader("Authorization") String token,
            @RequestBody Prescription prescription) {

        if (token == null || token.isEmpty()) {
            return ResponseEntity.status(401).body("Unauthorized request");
        }

        if (prescription == null) {
            return ResponseEntity.badRequest().body("Invalid prescription data");
        }

        return ResponseEntity.ok("Prescription saved successfully");
    }
}
