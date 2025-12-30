package com.ibm.smartclinic.backend.controller;

import com.ibm.smartclinic.backend.exception.ValidationException;
import com.ibm.smartclinic.backend.model.Prescription;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/prescriptions")
public class PrescriptionController {

    @PostMapping
    public ResponseEntity<String> savePrescription(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestBody Prescription prescription) {

        if (token == null || token.isEmpty()) {
            throw new ValidationException("Authorization header is required", "Authorization");
        }

        if (prescription == null) {
            throw new ValidationException("Invalid prescription data", "prescription");
        }

        if (prescription.getNotes() == null || prescription.getNotes().isEmpty()) {
            throw new ValidationException("Prescription notes are required", "notes");
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body("Prescription saved successfully");
    }
}
