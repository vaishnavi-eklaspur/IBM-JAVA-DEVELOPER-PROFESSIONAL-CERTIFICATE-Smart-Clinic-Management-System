package com.ibm.smartclinic.backend.dto;

public record PatientResponseDto(
        Long id,
        String name,
        String email,
        String phone
) {}
