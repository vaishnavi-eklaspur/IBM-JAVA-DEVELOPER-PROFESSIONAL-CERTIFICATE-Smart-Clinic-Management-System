package com.ibm.smartclinic.backend.controller;

import com.ibm.smartclinic.backend.dto.PrescriptionRequestDto;
import com.ibm.smartclinic.backend.dto.PrescriptionResponseDto;
import com.ibm.smartclinic.backend.exception.ResourceNotFoundException;
import com.ibm.smartclinic.backend.exception.ValidationException;
import com.ibm.smartclinic.backend.model.Doctor;
import com.ibm.smartclinic.backend.model.Patient;
import com.ibm.smartclinic.backend.service.DoctorService;
import com.ibm.smartclinic.backend.service.PatientService;
import com.ibm.smartclinic.backend.service.PrescriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/prescriptions")
@SecurityRequirement(name = "bearerAuth")
public class PrescriptionController {

    private final PrescriptionService prescriptionService;
    private final DoctorService doctorService;
    private final PatientService patientService;

    public PrescriptionController(@NonNull PrescriptionService prescriptionService,
                                  @NonNull DoctorService doctorService,
                                  @NonNull PatientService patientService) {
        this.prescriptionService = prescriptionService;
        this.doctorService = doctorService;
        this.patientService = patientService;
    }

    @Operation(summary = "Create a new prescription")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Prescription created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request body or validation error"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Appointment not found")
    })
    @PreAuthorize("hasRole('DOCTOR')")
    @PostMapping
    public ResponseEntity<PrescriptionResponseDto> savePrescription(@Valid @RequestBody PrescriptionRequestDto prescriptionDto) {
        Doctor doctor = resolveAuthenticatedDoctor();
        PrescriptionResponseDto responseDto = prescriptionService.createPrescription(doctor, prescriptionDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @Operation(summary = "Get prescriptions for authenticated doctor")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Prescriptions retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PreAuthorize("hasRole('DOCTOR')")
    @GetMapping("/doctor")
    public ResponseEntity<List<PrescriptionResponseDto>> getDoctorPrescriptions() {
        Doctor doctor = resolveAuthenticatedDoctor();
        return ResponseEntity.ok(prescriptionService.getPrescriptionsForDoctor(doctor.getId()));
    }

    @Operation(summary = "Get prescriptions for authenticated patient")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Prescriptions retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PreAuthorize("hasRole('PATIENT')")
    @GetMapping("/patient")
    public ResponseEntity<List<PrescriptionResponseDto>> getPatientPrescriptions() {
        Patient patient = resolveAuthenticatedPatient();
        return ResponseEntity.ok(prescriptionService.getPrescriptionsForPatient(patient.getId()));
    }

    private Doctor resolveAuthenticatedDoctor() {
        String email = getCurrentUserEmail();
        return doctorService.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", "email", email));
    }

    private Patient resolveAuthenticatedPatient() {
        String email = getCurrentUserEmail();
        return patientService.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", "email", email));
    }

    private String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new ValidationException("Authentication is required");
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof String email) {
            return email;
        }
        throw new ValidationException("Unsupported authentication principal type");
    }
}
