package com.ibm.smartclinic.backend.service;

import com.ibm.smartclinic.backend.model.Patient;
import com.ibm.smartclinic.backend.repository.PatientRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.lang.NonNull;

import java.util.Optional;

@Service
public class PatientService {

    private final PatientRepository patientRepository;
    private final PasswordEncoder passwordEncoder;

    public PatientService(@NonNull PatientRepository patientRepository,
                          @NonNull PasswordEncoder passwordEncoder) {
        this.patientRepository = patientRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @NonNull
    public Patient savePatientWithHashedPassword(@NonNull Patient patient) {
        if (patient.getPassword() != null) {
            patient.setPassword(passwordEncoder.encode(patient.getPassword()));
        }
        return patientRepository.save(patient);
    }

    /**
     * Spring Framework 6 guarantees non-null at runtime; warning is a known static-analysis limitation.
     */
    @NonNull
    public Optional<Patient> findByEmail(@NonNull String email) {
        return patientRepository.findByEmail(email);
    }

    /**
     * Spring Framework 6 guarantees non-null at runtime; warning is a known static-analysis limitation.
     */
    @NonNull
    public Page<Patient> getAllPatients(@NonNull Pageable pageable) {
        return patientRepository.findAll(pageable);
    }
}
