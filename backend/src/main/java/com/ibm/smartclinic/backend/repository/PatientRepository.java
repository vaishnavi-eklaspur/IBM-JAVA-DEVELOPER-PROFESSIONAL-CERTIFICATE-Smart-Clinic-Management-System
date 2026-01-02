package com.ibm.smartclinic.backend.repository;

import com.ibm.smartclinic.backend.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, Long> {

    Optional<Patient> findByEmail(@NonNull String email);

    Optional<Patient> findByEmailOrPhone(@NonNull String email, @Nullable String phone);
}
