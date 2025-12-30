package com.ibm.smartclinic.backend.controller;

import com.ibm.smartclinic.backend.model.Prescription;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

@RestController
@RequestMapping("/api/prescriptions")
public class PrescriptionController {

    @PostMapping
    public ResponseEntity<String> savePrescription(
            @RequestHeader(value = "Authorization", required = false) @NotBlank(message = "Authorization header is required") String token,
            @Valid @RequestBody Prescription prescription) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body("Prescription saved successfully");
    }
}
