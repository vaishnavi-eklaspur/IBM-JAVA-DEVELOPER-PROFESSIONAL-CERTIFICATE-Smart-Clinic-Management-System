package com.ibm.smartclinic.backend.controller;

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
            @PathVariable Long doctorId,
            @RequestParam String date) {

        return ResponseEntity.ok(
                doctorService.getAvailableTimeSlots(
                        doctorId,
                        LocalDate.parse(date)
                )
        );
    }
    @GetMapping("/search")
public ResponseEntity<?> searchDoctors(
        @RequestParam String speciality,
        @RequestParam String time
) {
    return ResponseEntity.ok(
            doctorService.getAllDoctors().stream()
                    .filter(d -> d.getSpeciality().equalsIgnoreCase(speciality))
                    .toList()
    );
}

}
