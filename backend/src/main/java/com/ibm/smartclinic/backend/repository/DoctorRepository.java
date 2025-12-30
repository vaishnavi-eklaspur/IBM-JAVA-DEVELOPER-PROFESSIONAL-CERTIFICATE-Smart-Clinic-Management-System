package com.ibm.smartclinic.backend.repository;

import com.ibm.smartclinic.backend.model.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {
	Optional<Doctor> findByEmail(String email);

	Page<Doctor> findAll(Pageable pageable);
	Page<Doctor> findBySpecialityIgnoreCase(String speciality, Pageable pageable);
}
