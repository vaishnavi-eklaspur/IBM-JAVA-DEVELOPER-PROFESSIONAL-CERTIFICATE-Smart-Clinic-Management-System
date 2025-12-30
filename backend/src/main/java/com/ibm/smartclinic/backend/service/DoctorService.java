import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
    public Page<Doctor> getAllDoctors(Pageable pageable) {
        return doctorRepository.findAll(pageable);
    }

    public Page<Doctor> findBySpeciality(String speciality, Pageable pageable) {
        return doctorRepository.findBySpecialityIgnoreCase(speciality, pageable);
    }
package com.ibm.smartclinic.backend.service;

import com.ibm.smartclinic.backend.exception.ResourceNotFoundException;
import com.ibm.smartclinic.backend.exception.ValidationException;
import com.ibm.smartclinic.backend.model.Doctor;
import com.ibm.smartclinic.backend.repository.DoctorRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public DoctorService(DoctorRepository doctorRepository, PasswordEncoder passwordEncoder) {
        this.doctorRepository = doctorRepository;
        this.passwordEncoder = passwordEncoder;
    }

    
    public List<Doctor> getAllDoctors() {
        return doctorRepository.findAll();
    }

   
    public List<LocalTime> getAvailableTimeSlots(Long doctorId, LocalDate date) {
        // Static availability for lab/demo purposes
        return List.of(
                LocalTime.of(10, 0),
                LocalTime.of(11, 0),
                LocalTime.of(14, 0)
        );
    }

    
    public ResponseEntity<String> validateDoctorLogin(String email, String password) {
        if (email == null || password == null) {
            throw new ValidationException("Email and password are required");
        }
        return doctorRepository.findByEmail(email)
                .map(doctor -> passwordEncoder.matches(password, doctor.getPassword())
                        ? ResponseEntity.ok("Doctor login successful")
                        : ResponseEntity.status(401).body("Invalid credentials"))
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", "email", email));
    }

    public Doctor saveDoctorWithHashedPassword(Doctor doctor) {
        if (doctor.getPassword() != null) {
            doctor.setPassword(passwordEncoder.encode(doctor.getPassword()));
        }
        return doctorRepository.save(doctor);
    }
}
