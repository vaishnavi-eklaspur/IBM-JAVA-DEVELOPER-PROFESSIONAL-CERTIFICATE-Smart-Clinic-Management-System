package com.ibm.smartclinic.backend;

import static org.assertj.core.api.Assertions.assertThat;

import com.ibm.smartclinic.backend.model.Doctor;
import com.ibm.smartclinic.backend.repository.DoctorRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootTest
class DoctorAuthenticationFlowTest {

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void seededDoctorHasUsablePassword() {
        Doctor doctor = doctorRepository.findByEmail("john@clinic.com").orElse(null);
        assertThat(doctor).isNotNull();
        assertThat(passwordEncoder.matches("password", doctor.getPassword())).isTrue();
    }
}