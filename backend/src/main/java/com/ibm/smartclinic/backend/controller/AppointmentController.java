package com.ibm.smartclinic.backend.controller;

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
import java.util.List;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @GetMapping("/patient/{patientId}")
        public ResponseEntity<List<AppointmentResponseDto>> getAppointmentsByPatient(
            @PathVariable @Min(value = 1, message = "Patient ID must be positive") Long patientId) {
        List<AppointmentResponseDto> dtos = appointmentService
            .getAppointmentsForDoctorOnDate(patientId, null)
            .stream().map(this::toAppointmentResponseDto).toList();
        return ResponseEntity.ok(dtos);
        }

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
