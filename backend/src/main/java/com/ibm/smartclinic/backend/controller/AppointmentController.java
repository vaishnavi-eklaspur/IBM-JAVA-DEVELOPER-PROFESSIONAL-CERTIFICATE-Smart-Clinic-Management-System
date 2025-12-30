package com.ibm.smartclinic.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import com.ibm.smartclinic.backend.model.Appointment;
import com.ibm.smartclinic.backend.model.Doctor;
import com.ibm.smartclinic.backend.dto.AppointmentResponseDto;
import com.ibm.smartclinic.backend.dto.DoctorResponseDto;
import com.ibm.smartclinic.backend.service.AppointmentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @Operation(summary = "Get paginated list of appointments for a patient", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "List of appointments returned"),
        @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Patient not found")
    })
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<Page<AppointmentResponseDto>> getAppointmentsByPatient(
            @PathVariable @Min(value = 1, message = "Patient ID must be positive") Long patientId,
            @PageableDefault(size = 20, sort = "appointmentTime") Pageable pageable) {
        Page<Appointment> page = appointmentService.getAppointmentsByPatientId(patientId, pageable);
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
    @PostMapping
    public ResponseEntity<AppointmentResponseDto> bookAppointment(
            @Valid @RequestBody Appointment appointment) {
        Appointment booked = appointmentService.bookAppointment(appointment);
        return ResponseEntity.status(HttpStatus.CREATED).body(toAppointmentResponseDto(booked));
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
}
