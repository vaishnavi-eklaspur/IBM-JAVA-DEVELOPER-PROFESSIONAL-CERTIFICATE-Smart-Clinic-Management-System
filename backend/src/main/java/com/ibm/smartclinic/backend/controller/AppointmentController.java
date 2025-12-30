package com.ibm.smartclinic.backend.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import com.ibm.smartclinic.backend.model.Appointment;
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
    public ResponseEntity<List<Appointment>> getAppointmentsByPatient(
            @PathVariable @Min(value = 1, message = "Patient ID must be positive") Long patientId) {
        List<Appointment> appointments = appointmentService
                .getAppointmentsForDoctorOnDate(patientId, null);
        return ResponseEntity.ok(appointments);
    }

    @PostMapping
    public ResponseEntity<Appointment> bookAppointment(
            @Valid @RequestBody Appointment appointment) {
        Appointment booked = appointmentService.bookAppointment(appointment);
        return ResponseEntity.status(HttpStatus.CREATED).body(booked);
    }
}
