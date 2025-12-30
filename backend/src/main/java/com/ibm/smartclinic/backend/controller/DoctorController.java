package com.ibm.smartclinic.backend.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import com.ibm.smartclinic.backend.model.Doctor;
import com.ibm.smartclinic.backend.service.DoctorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/doctors")
public class DoctorController {

    private final DoctorService doctorService;

    public DoctorController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }

    
    @GetMapping
    public ResponseEntity<List<Doctor>> getAllDoctors() {
        return ResponseEntity.ok(doctorService.getAllDoctors());
    }

    @GetMapping("/{doctorId}/availability")
        public ResponseEntity<?> getDoctorAvailability(
            @PathVariable @Min(value = 1, message = "Doctor ID must be positive") Long doctorId,
            @RequestParam @NotBlank(message = "Date parameter is required") String date) {
        return ResponseEntity.ok(
            doctorService.getAvailableTimeSlots(
                doctorId,
                LocalDate.parse(date)
            )
        );
        }

    @GetMapping("/search")
    public ResponseEntity<?> searchDoctors(
            @RequestParam @NotBlank(message = "Speciality parameter is required") String speciality,
            @RequestParam(required = false) String time) {
        return ResponseEntity.ok(
                doctorService.getAllDoctors().stream()
                        .filter(d -> d.getSpeciality().equalsIgnoreCase(speciality))
                        .toList()
        );
    }

}
