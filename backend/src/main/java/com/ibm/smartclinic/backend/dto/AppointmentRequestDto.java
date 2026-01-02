package com.ibm.smartclinic.backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * Minimal request DTO for booking appointments.
 * Keeps controller defensive and avoids binding JPA entities directly.
 */
public class AppointmentRequestDto {

    @NotNull(message = "Appointment time is required")
    private LocalDateTime appointmentTime;

    @NotNull(message = "Doctor ID is required")
    @Min(value = 1, message = "Doctor ID must be positive")
    private Long doctorId;

    public LocalDateTime getAppointmentTime() {
        return appointmentTime;
    }

    public void setAppointmentTime(LocalDateTime appointmentTime) {
        this.appointmentTime = appointmentTime;
    }

    public Long getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Long doctorId) {
        this.doctorId = doctorId;
    }
}
