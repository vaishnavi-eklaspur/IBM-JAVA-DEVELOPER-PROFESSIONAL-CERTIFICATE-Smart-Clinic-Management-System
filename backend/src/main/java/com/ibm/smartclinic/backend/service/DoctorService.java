package com.ibm.smartclinic.backend.service;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class DoctorService {

    public List<LocalTime> getAvailableTimeSlots(Long doctorId, LocalDate date) {
        // Sample static availability for demonstration
        return List.of(
                LocalTime.of(10, 0),
                LocalTime.of(11, 0),
                LocalTime.of(14, 0)
        );
    }

    public ResponseEntity<String> validateDoctorLogin(String email, String password) {
        // Simplified login validation for lab
        if (email != null && password != null) {
            return ResponseEntity.ok("Doctor login successful");
        }
        return ResponseEntity.badRequest().body("Invalid credentials");
    }
}
