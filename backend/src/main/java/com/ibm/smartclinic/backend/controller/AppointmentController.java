package com.ibm.smartclinic.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import com.ibm.smartclinic.backend.dto.AppointmentRequestDto;
import com.ibm.smartclinic.backend.exception.ResourceNotFoundException;
import com.ibm.smartclinic.backend.exception.ValidationException;
import com.ibm.smartclinic.backend.model.Appointment;
import com.ibm.smartclinic.backend.model.Doctor;
import com.ibm.smartclinic.backend.dto.AppointmentResponseDto;
import com.ibm.smartclinic.backend.dto.DoctorResponseDto;
import com.ibm.smartclinic.backend.service.AppointmentService;
import com.ibm.smartclinic.backend.service.DoctorService;
import com.ibm.smartclinic.backend.service.PatientService;
import com.ibm.smartclinic.backend.model.Patient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import java.util.List;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final DoctorService doctorService;
    private final PatientService patientService;

    public AppointmentController(AppointmentService appointmentService,
                                 DoctorService doctorService,
                                 PatientService patientService) {
        this.appointmentService = appointmentService;
        this.doctorService = doctorService;
        this.patientService = patientService;
    }

    @Operation(summary = "Get paginated list of appointments for a patient", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "List of appointments returned"),
        @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Patient not found")
    })
    @PreAuthorize("hasRole('PATIENT')")
    @GetMapping("/patient/{patientId}")
        public ResponseEntity<Page<AppointmentResponseDto>> getAppointmentsByPatient(
            @PathVariable @Min(value = 1, message = "Patient ID must be positive") @NonNull Long patientId,
            @PageableDefault(size = 20, sort = "appointmentTime") @NonNull Pageable pageable) {
        Patient patient = resolveAuthenticatedPatient();
        if (!patient.getId().equals(patientId)) {
            throw new ValidationException("Authenticated patient does not match requested patient ID");
        }
        Page<Appointment> page = appointmentService.getAppointmentsByPatientId(patient.getId(), pageable);
        Page<AppointmentResponseDto> dtoPage = page.map(this::toAppointmentResponseDto);
        return ResponseEntity.ok(dtoPage);
    }

    @Operation(summary = "Book a new appointment", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Appointment booked successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request body or validation error"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "409", description = "Doctor already booked at this time")
    })
    @PreAuthorize("hasRole('PATIENT')")
    @PostMapping
        public ResponseEntity<AppointmentResponseDto> bookAppointment(
                @Valid @RequestBody @NonNull AppointmentRequestDto requestDto) {
            Patient patient = resolveAuthenticatedPatient();
            Doctor doctor = doctorService.requireById(requestDto.getDoctorId());

            Appointment appointment = new Appointment();
            appointment.setAppointmentTime(requestDto.getAppointmentTime());
            appointment.setDoctor(doctor);
            appointment.setPatient(patient);

            Appointment booked = appointmentService.bookAppointment(appointment);
            return ResponseEntity.status(HttpStatus.CREATED).body(toAppointmentResponseDto(booked));
        }

        @Operation(summary = "Get authenticated doctor's appointments", security = @SecurityRequirement(name = "bearerAuth"))
        @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Appointments returned"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
        })
        @PreAuthorize("hasRole('DOCTOR')")
        @GetMapping("/doctor")
        public ResponseEntity<List<AppointmentResponseDto>> getAppointmentsForDoctor() {
            Doctor doctor = resolveAuthenticatedDoctor();
            List<AppointmentResponseDto> responses = appointmentService.getAppointmentsForDoctor(doctor.getId())
                    .stream()
                    .map(this::toAppointmentResponseDto)
                    .toList();
            return ResponseEntity.ok(responses);
        }

    @Operation(summary = "Mark appointment as completed", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Appointment completed"),
        @ApiResponse(responseCode = "400", description = "Invalid transition"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Appointment not found")
    })
    @PreAuthorize("hasRole('DOCTOR')")
    @PostMapping("/{appointmentId}/complete")
    public ResponseEntity<AppointmentResponseDto> completeAppointment(
            @PathVariable @Min(value = 1, message = "Appointment ID must be positive") @NonNull Long appointmentId) {
        Doctor doctor = resolveAuthenticatedDoctor();
        Appointment updated = appointmentService.completeAppointment(appointmentId, doctor);
        return ResponseEntity.ok(toAppointmentResponseDto(updated));
    }

    @Operation(summary = "Cancel appointment", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Appointment cancelled"),
        @ApiResponse(responseCode = "400", description = "Invalid transition"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Appointment not found")
    })
    @PreAuthorize("hasRole('DOCTOR')")
    @PostMapping("/{appointmentId}/cancel")
    public ResponseEntity<AppointmentResponseDto> cancelAppointment(
            @PathVariable @Min(value = 1, message = "Appointment ID must be positive") @NonNull Long appointmentId) {
        Doctor doctor = resolveAuthenticatedDoctor();
        Appointment updated = appointmentService.cancelAppointment(appointmentId, doctor);
        return ResponseEntity.ok(toAppointmentResponseDto(updated));
    }

    private AppointmentResponseDto toAppointmentResponseDto(Appointment appointment) {
        Doctor doctor = appointment.getDoctor();
        DoctorResponseDto doctorDto = doctor != null ? new DoctorResponseDto(
                doctor.getId(), doctor.getName(), doctor.getEmail(), doctor.getSpeciality()) : null;
        return new AppointmentResponseDto(
                appointment.getId(),
                appointment.getAppointmentTime(),
                doctorDto,
                appointment.getPatient() != null ? appointment.getPatient().getId() : null,
                appointment.getStatus() != null ? appointment.getStatus().name() : null
        );
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
