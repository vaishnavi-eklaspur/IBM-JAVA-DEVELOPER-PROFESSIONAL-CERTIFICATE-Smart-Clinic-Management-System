package com.ibm.smartclinic.backend.repository;

import com.ibm.smartclinic.backend.model.Prescription;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;

public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {

    @NonNull
    List<Prescription> findByAppointmentDoctorId(@NonNull Long doctorId);

    @NonNull
    List<Prescription> findByAppointmentPatientId(@NonNull Long patientId);
}
