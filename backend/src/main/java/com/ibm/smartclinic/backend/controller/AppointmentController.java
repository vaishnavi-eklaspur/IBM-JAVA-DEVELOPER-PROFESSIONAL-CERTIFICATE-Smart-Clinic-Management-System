package com.ibm.smartclinic.backend.controller;


import com.ibm.smartclinic.backend.model.Appointment;
import com.ibm.smartclinic.backend.service.AppointmentService;
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
        // In a real app, filter by patientId
        List<Appointment> appointments = appointmentService
                .getAppointmentsForDoctorOnDate(patientId, null); // Placeholder, should be by patient
        return ResponseEntity.ok(appointments);
    }
}
