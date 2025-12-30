package com.ibm.smartclinic.backend.repository;

import com.ibm.smartclinic.backend.model.Appointment;
import com.ibm.smartclinic.backend.model.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    Optional<Appointment> findByDoctorAndAppointmentTime(
            Doctor doctor,
            LocalDateTime appointmentTime
    );
}
