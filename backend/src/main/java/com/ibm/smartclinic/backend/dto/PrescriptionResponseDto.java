package com.ibm.smartclinic.backend.dto;

public class PrescriptionResponseDto {
    private Long id;
    private String notes;
    private Long appointmentId;

    public PrescriptionResponseDto() {}

    public PrescriptionResponseDto(Long id, String notes, Long appointmentId) {
        this.id = id;
        this.notes = notes;
        this.appointmentId = appointmentId;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public Long getAppointmentId() { return appointmentId; }
    public void setAppointmentId(Long appointmentId) { this.appointmentId = appointmentId; }
}