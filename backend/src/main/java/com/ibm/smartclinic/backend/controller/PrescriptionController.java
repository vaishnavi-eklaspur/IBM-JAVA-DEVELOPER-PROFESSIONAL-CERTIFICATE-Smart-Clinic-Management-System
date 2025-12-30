package com.ibm.smartclinic.backend.controller;

import com.ibm.smartclinic.backend.model.Prescription;
import com.ibm.smartclinic.backend.model.Appointment;
import com.ibm.smartclinic.backend.dto.PrescriptionRequestDto;
import com.ibm.smartclinic.backend.dto.PrescriptionResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

@RestController
@RequestMapping("/api/prescriptions")
public class PrescriptionController {

    @PostMapping
    public ResponseEntity<PrescriptionResponseDto> savePrescription(
            @RequestHeader(value = "Authorization", required = false) @NotBlank(message = "Authorization header is required") String token,
            @Valid @RequestBody PrescriptionRequestDto prescriptionDto) {
        // Simulate mapping and persistence (service layer would handle this in a real app)
        Prescription prescription = new Prescription();
        prescription.setNotes(prescriptionDto.getNotes());
        Appointment appointment = new Appointment();
        appointment.setId(prescriptionDto.getAppointmentId());
        prescription.setAppointment(appointment);
        // Simulate ID assignment
        prescription.setId(1L);
        PrescriptionResponseDto responseDto = toPrescriptionResponseDto(prescription);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    private PrescriptionResponseDto toPrescriptionResponseDto(Prescription prescription) {
        return new PrescriptionResponseDto(
                prescription.getId(),
                prescription.getNotes(),
                prescription.getAppointment() != null ? prescription.getAppointment().getId() : null
        );
    }
}
