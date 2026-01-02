package com.ibm.smartclinic.backend.config;

import com.ibm.smartclinic.backend.model.Doctor;
import com.ibm.smartclinic.backend.repository.DoctorRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

@Configuration
public class DataLoader {

    @Bean
    @DependsOn("flywayInitializer")
    CommandLineRunner loadDoctors(DoctorRepository doctorRepository) {
        return args -> {

            if (doctorRepository.count() == 0) {

                Doctor d1 = new Doctor();
                d1.setName("Dr John Smith");
                d1.setEmail("john@clinic.com");
                d1.setSpeciality("Cardiology");

                Doctor d2 = new Doctor();
                d2.setName("Dr Alice Brown");
                d2.setEmail("alice@clinic.com");
                d2.setSpeciality("Dermatology");

                doctorRepository.save(d1);
                doctorRepository.save(d2);
            }
        };
    }
}
