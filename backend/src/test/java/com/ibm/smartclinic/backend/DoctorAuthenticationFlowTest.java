package com.ibm.smartclinic.backend;

import static org.assertj.core.api.Assertions.assertThat;

import com.ibm.smartclinic.backend.model.Doctor;
import com.ibm.smartclinic.backend.model.UserIdentity;
import com.ibm.smartclinic.backend.model.UserRole;
import com.ibm.smartclinic.backend.service.DoctorService;
import com.ibm.smartclinic.backend.service.IdentityService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class DoctorAuthenticationFlowTest {

    @Autowired
    private IdentityService identityService;

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void seededDoctorHasUsablePassword() {
        UserIdentity identity = identityService.registerIdentity("john@clinic.com", "password", UserRole.DOCTOR);

        Doctor doctor = new Doctor();
        doctor.setName("Dr John Smith");
        doctor.setEmail(identity.getEmail());
        doctor.setSpeciality("Cardiology");
        doctor.setPassword("password");

        Doctor saved = doctorService.saveDoctorWithHashedPassword(doctor);

        assertThat(passwordEncoder.matches("password", saved.getPassword())).isTrue();
        assertThat(identityService.authenticate("john@clinic.com", "password", UserRole.DOCTOR)).isNotNull();
    }
}