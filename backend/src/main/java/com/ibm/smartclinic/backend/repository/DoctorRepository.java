package com.ibm.smartclinic.backend.repository;

import com.ibm.smartclinic.backend.model.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {
}
