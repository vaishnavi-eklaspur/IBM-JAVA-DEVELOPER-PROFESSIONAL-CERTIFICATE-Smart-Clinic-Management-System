package com.ibm.smartclinic.backend.repository;

import com.ibm.smartclinic.backend.model.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {
	Optional<Doctor> findByEmail(@NonNull String email);

	@NonNull
	Page<Doctor> findAll(@NonNull Pageable pageable);

	@NonNull
	Page<Doctor> findBySpecialityIgnoreCase(@NonNull String speciality, @NonNull Pageable pageable);
}
