package com.ibm.smartclinic.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class PrescriptionRequestDto {
    @NotBlank(message = "Prescription notes must not be blank")
    private String notes;
    @NotNull(message = "Appointment ID is required")
    private Long appointmentId;

    public PrescriptionRequestDto() {}

    public PrescriptionRequestDto(String notes, Long appointmentId) {
        this.notes = notes;
        this.appointmentId = appointmentId;
    }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public Long getAppointmentId() { return appointmentId; }
    public void setAppointmentId(Long appointmentId) { this.appointmentId = appointmentId; }
}