package com.ibm.smartclinic.backend.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import com.ibm.smartclinic.backend.model.Doctor;
import com.ibm.smartclinic.backend.dto.DoctorResponseDto;
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
    public ResponseEntity<List<DoctorResponseDto>> getAllDoctors() {
        List<DoctorResponseDto> dtos = doctorService.getAllDoctors().stream()
                .map(this::toDoctorResponseDto)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{doctorId}/availability")
        public ResponseEntity<List<String>> getDoctorAvailability(
                @PathVariable @Min(value = 1, message = "Doctor ID must be positive") Long doctorId,
                @RequestParam @NotBlank(message = "Date parameter is required") String date) {
            // Returns available time slots as strings (e.g., "10:00")
            return ResponseEntity.ok(
                doctorService.getAvailableTimeSlots(
                    doctorId,
                    LocalDate.parse(date)
                ).stream().map(Object::toString).toList()
            );
        }

    @GetMapping("/search")
        public ResponseEntity<List<DoctorResponseDto>> searchDoctors(
            @RequestParam @NotBlank(message = "Speciality parameter is required") String speciality,
            @RequestParam(required = false) String time) {
        List<DoctorResponseDto> dtos = doctorService.getAllDoctors().stream()
            .filter(d -> d.getSpeciality().equalsIgnoreCase(speciality))
            .map(this::toDoctorResponseDto)
            .toList();
        return ResponseEntity.ok(dtos);
        }

        private DoctorResponseDto toDoctorResponseDto(Doctor doctor) {
        return new DoctorResponseDto(
            doctor.getId(),
            doctor.getName(),
            doctor.getEmail(),
            doctor.getSpeciality()
        );
        }

}
