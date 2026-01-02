package com.ibm.smartclinic.backend.repository;

import com.ibm.smartclinic.backend.model.Appointment;
import com.ibm.smartclinic.backend.model.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
        Optional<Appointment> findByDoctorAndAppointmentTime(
            @NonNull Doctor doctor,
            @NonNull LocalDateTime appointmentTime
        );

        @NonNull
        Page<Appointment> findAll(@NonNull Pageable pageable);

        @NonNull
        Page<Appointment> findByPatientId(@NonNull Long patientId, @NonNull Pageable pageable);

        @NonNull
        List<Appointment> findByPatientIdOrderByAppointmentTimeDesc(@NonNull Long patientId);

        @NonNull
        List<Appointment> findByDoctorIdOrderByAppointmentTimeDesc(@NonNull Long doctorId);
}
