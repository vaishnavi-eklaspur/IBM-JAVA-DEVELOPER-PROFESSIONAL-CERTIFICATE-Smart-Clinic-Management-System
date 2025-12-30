package com.ibm.smartclinic.backend.dto;

import java.time.LocalDateTime;

public class AppointmentResponseDto {
    private Long id;
    private LocalDateTime appointmentTime;
    private DoctorResponseDto doctor;
    private Long patientId;
    private String status;

    public AppointmentResponseDto() {}

    public AppointmentResponseDto(Long id, LocalDateTime appointmentTime, DoctorResponseDto doctor, Long patientId, String status) {
        this.id = id;
        this.appointmentTime = appointmentTime;
        this.doctor = doctor;
        this.patientId = patientId;
        this.status = status;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDateTime getAppointmentTime() { return appointmentTime; }
    public void setAppointmentTime(LocalDateTime appointmentTime) { this.appointmentTime = appointmentTime; }
    public DoctorResponseDto getDoctor() { return doctor; }
    public void setDoctor(DoctorResponseDto doctor) { this.doctor = doctor; }
    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}