package com.ibm.smartclinic.backend.service;

import com.ibm.smartclinic.backend.model.Doctor;
import com.ibm.smartclinic.backend.repository.DoctorRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class DoctorService {

    private final DoctorRepository doctorRepository;

    public DoctorService(DoctorRepository doctorRepository) {
        this.doctorRepository = doctorRepository;
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
        if (email != null && password != null) {
            return ResponseEntity.ok("Doctor login successful");
        }
        return ResponseEntity.badRequest().body("Invalid credentials");
    }
}
