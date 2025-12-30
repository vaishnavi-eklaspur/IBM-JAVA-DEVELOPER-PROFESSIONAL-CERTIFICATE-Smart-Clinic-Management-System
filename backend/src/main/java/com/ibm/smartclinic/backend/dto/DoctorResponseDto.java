package com.ibm.smartclinic.backend.dto;

public class DoctorResponseDto {
    private Long id;
    private String name;
    private String email;
    private String speciality;

    public DoctorResponseDto() {}

    public DoctorResponseDto(Long id, String name, String email, String speciality) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.speciality = speciality;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getSpeciality() { return speciality; }
    public void setSpeciality(String speciality) { this.speciality = speciality; }
}