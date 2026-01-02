package com.ibm.smartclinic.backend.service;

import com.ibm.smartclinic.backend.exception.ResourceNotFoundException;
import com.ibm.smartclinic.backend.exception.ValidationException;
import com.ibm.smartclinic.backend.model.Doctor;
import com.ibm.smartclinic.backend.repository.DoctorRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.lang.NonNull;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public DoctorService(@NonNull DoctorRepository doctorRepository,
                         @NonNull PasswordEncoder passwordEncoder) {
        this.doctorRepository = doctorRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Spring Framework 6 guarantees non-null at runtime; warning is a known static-analysis limitation.
     */
    @NonNull
    public Page<Doctor> getAllDoctors(@NonNull Pageable pageable) {
        return doctorRepository.findAll(pageable);
    }

    /**
     * Spring Framework 6 guarantees non-null at runtime; warning is a known static-analysis limitation.
     */
    @NonNull
    public Page<Doctor> findBySpeciality(@NonNull String speciality, @NonNull Pageable pageable) {
        return doctorRepository.findBySpecialityIgnoreCase(speciality, pageable);
    }

   
    @NonNull
    public List<LocalTime> getAvailableTimeSlots(@NonNull Long doctorId, @NonNull LocalDate date) {
        // Static availability for lab/demo purposes
        return List.of(
                LocalTime.of(10, 0),
                LocalTime.of(11, 0),
                LocalTime.of(14, 0)
        );
    }

    
    @NonNull
    public ResponseEntity<String> validateDoctorLogin(@NonNull String email, @NonNull String password) {
        if (email == null || password == null) {
            throw new ValidationException("Email and password are required");
        }
        return doctorRepository.findByEmail(email)
                .map(doctor -> passwordEncoder.matches(password, doctor.getPassword())
                        ? ResponseEntity.ok("Doctor login successful")
                        : ResponseEntity.status(401).body("Invalid credentials"))
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", "email", email));
    }

    @NonNull
    public Doctor saveDoctorWithHashedPassword(@NonNull Doctor doctor) {
        if (doctor.getPassword() != null) {
            doctor.setPassword(passwordEncoder.encode(doctor.getPassword()));
        }
        return doctorRepository.save(doctor);
    }

    /**
     * Spring Framework 6 guarantees non-null at runtime; warning is a known static-analysis limitation.
     */
    @NonNull
    public Optional<Doctor> findByEmail(@NonNull String email) {
        return doctorRepository.findByEmail(email);
    }
}
