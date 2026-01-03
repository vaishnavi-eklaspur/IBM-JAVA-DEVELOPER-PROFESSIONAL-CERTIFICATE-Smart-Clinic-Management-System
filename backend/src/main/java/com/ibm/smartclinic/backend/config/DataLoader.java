package com.ibm.smartclinic.backend.config;

import com.ibm.smartclinic.backend.model.Doctor;
import com.ibm.smartclinic.backend.model.UserIdentity;
import com.ibm.smartclinic.backend.model.UserRole;
import com.ibm.smartclinic.backend.repository.DoctorRepository;
import com.ibm.smartclinic.backend.service.IdentityService;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@Profile("dev")
public class DataLoader {

    private static final String DEFAULT_DOCTOR_PASSWORD = "password";

    @Bean
    @DependsOn("flywayInitializer")
    CommandLineRunner loadDoctors(DoctorRepository doctorRepository,
                                 PasswordEncoder passwordEncoder,
                                 IdentityService identityService) {
        return args -> {
            List<SeedDoctor> seedDoctors = List.of(
                    new SeedDoctor("Dr John Smith", "john@clinic.com", "Cardiology"),
                    new SeedDoctor("Dr Alice Brown", "alice@clinic.com", "Dermatology")
            );

            for (SeedDoctor seed : seedDoctors) {
                UserIdentity identity = identityService.provisionIdentity(seed.email(), UserRole.DOCTOR, DEFAULT_DOCTOR_PASSWORD);

                Doctor doctor = doctorRepository.findByEmail(identity.getEmail()).orElseGet(Doctor::new);
                doctor.setName(seed.name());
                doctor.setEmail(identity.getEmail());
                doctor.setSpeciality(seed.speciality());

                if (needsPasswordRefresh(doctor.getPassword(), passwordEncoder)) {
                    doctor.setPassword(passwordEncoder.encode(DEFAULT_DOCTOR_PASSWORD));
                }

                doctorRepository.save(doctor);
            }
        };
    }

    private boolean needsPasswordRefresh(String existingPassword, PasswordEncoder passwordEncoder) {
        if (existingPassword == null || existingPassword.isBlank()) {
            return true;
        }
        if (!isBcrypt(existingPassword)) {
            return true;
        }
        return !passwordEncoder.matches(DEFAULT_DOCTOR_PASSWORD, existingPassword);
    }

    private boolean isBcrypt(String value) {
        return value.startsWith("$2a$") || value.startsWith("$2b$") || value.startsWith("$2y$");
    }

    private record SeedDoctor(String name, String email, String speciality) {}
}
