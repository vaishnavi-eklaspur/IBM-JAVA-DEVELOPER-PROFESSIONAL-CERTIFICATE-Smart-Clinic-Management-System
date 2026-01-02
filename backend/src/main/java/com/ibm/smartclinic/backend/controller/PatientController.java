package com.ibm.smartclinic.backend.controller;

import com.ibm.smartclinic.backend.dto.AppointmentResponseDto;
import com.ibm.smartclinic.backend.dto.DoctorResponseDto;
import com.ibm.smartclinic.backend.dto.PatientResponseDto;
import com.ibm.smartclinic.backend.dto.PrescriptionResponseDto;
import com.ibm.smartclinic.backend.exception.ResourceNotFoundException;
import com.ibm.smartclinic.backend.exception.ValidationException;
import com.ibm.smartclinic.backend.model.Appointment;
import com.ibm.smartclinic.backend.model.Doctor;
import com.ibm.smartclinic.backend.model.Patient;
import com.ibm.smartclinic.backend.service.AppointmentService;
import com.ibm.smartclinic.backend.service.PatientService;
import com.ibm.smartclinic.backend.service.PrescriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/patient")
@SecurityRequirement(name = "bearerAuth")
public class PatientController {

    private final PatientService patientService;
    private final AppointmentService appointmentService;
    private final PrescriptionService prescriptionService;

    public PatientController(@NonNull PatientService patientService,
                             @NonNull AppointmentService appointmentService,
                             @NonNull PrescriptionService prescriptionService) {
        this.patientService = patientService;
        this.appointmentService = appointmentService;
        this.prescriptionService = prescriptionService;
    }

    @Operation(summary = "Get authenticated patient profile")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Profile returned"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PreAuthorize("hasRole('PATIENT')")
    @GetMapping("/profile")
    public ResponseEntity<PatientResponseDto> getProfile() {
        Patient patient = resolveAuthenticatedPatient();
        PatientResponseDto response = new PatientResponseDto(
                patient.getId(),
                patient.getName(),
                patient.getEmail(),
                patient.getPhone()
        );
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get authenticated patient's appointments")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Appointments returned"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PreAuthorize("hasRole('PATIENT')")
    @GetMapping("/appointments")
    public ResponseEntity<List<AppointmentResponseDto>> getAppointments() {
        Patient patient = resolveAuthenticatedPatient();
        List<AppointmentResponseDto> responses = appointmentService.getAppointmentsForPatient(patient.getId())
                .stream()
                .map(this::toAppointmentResponseDto)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "Get authenticated patient's prescriptions")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Prescriptions returned"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PreAuthorize("hasRole('PATIENT')")
    @GetMapping("/prescriptions")
    public ResponseEntity<List<PrescriptionResponseDto>> getPrescriptions() {
        Patient patient = resolveAuthenticatedPatient();
        return ResponseEntity.ok(prescriptionService.getPrescriptionsForPatient(patient.getId()));
    }

    private AppointmentResponseDto toAppointmentResponseDto(@NonNull Appointment appointment) {
        Doctor doctor = appointment.getDoctor();
        DoctorResponseDto doctorDto = doctor != null ? new DoctorResponseDto(
                doctor.getId(),
                doctor.getName(),
                doctor.getEmail(),
                doctor.getSpeciality()
        ) : null;
        return new AppointmentResponseDto(
                appointment.getId(),
                appointment.getAppointmentTime(),
                doctorDto,
                appointment.getPatient() != null ? appointment.getPatient().getId() : null,
                appointment.getStatus() != null ? appointment.getStatus().name() : null
        );
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
