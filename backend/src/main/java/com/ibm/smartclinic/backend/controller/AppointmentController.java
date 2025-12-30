package com.ibm.smartclinic.backend.controller;

import com.ibm.smartclinic.backend.exception.ResourceNotFoundException;
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
            @PathVariable Long patientId) {
        if (patientId == null || patientId <= 0) {
            throw new ResourceNotFoundException("Patient", "id", patientId);
        }
        List<Appointment> appointments = appointmentService
                .getAppointmentsForDoctorOnDate(patientId, null);
        return ResponseEntity.ok(appointments);
    }

    @PostMapping
    public ResponseEntity<Appointment> bookAppointment(
            @RequestBody Appointment appointment) {
        Appointment booked = appointmentService.bookAppointment(appointment);
        return ResponseEntity.status(HttpStatus.CREATED).body(booked);
    }
}
