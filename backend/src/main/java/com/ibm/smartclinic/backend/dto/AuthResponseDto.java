package com.ibm.smartclinic.backend.dto;

public class AuthResponseDto {

    private final String token;
    private final String role;

    public AuthResponseDto(String token, String role) {
        this.token = token;
        this.role = role;
    }

    public String getToken() {
        return token;
    }

    public String getRole() {
        return role;
    }
}
