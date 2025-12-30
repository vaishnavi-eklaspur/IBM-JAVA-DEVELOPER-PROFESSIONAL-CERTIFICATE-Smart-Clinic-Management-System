package com.ibm.smartclinic.backend.service;

import com.ibm.smartclinic.backend.model.Appointment;
import com.ibm.smartclinic.backend.model.AppointmentStatus;
import com.ibm.smartclinic.backend.model.Doctor;
import com.ibm.smartclinic.backend.repository.AppointmentRepository;
import com.ibm.smartclinic.backend.repository.PatientRepository;
import com.ibm.smartclinic.backend.repository.DoctorRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;

    public AppointmentService(AppointmentRepository appointmentRepository, DoctorRepository doctorRepository, PatientRepository patientRepository) {
        this.appointmentRepository = appointmentRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
    }

    public Appointment bookAppointment(Appointment appointment) {
        // Conflict detection: prevent double-booking same doctor at same time
        if (appointment.getDoctor() == null || appointment.getAppointmentTime() == null) {
            throw new IllegalArgumentException("Doctor and appointment time must be provided");
        }
        boolean conflict = appointmentRepository.findByDoctorAndAppointmentTime(
                appointment.getDoctor(), appointment.getAppointmentTime()
        ).isPresent();
        if (conflict) {
            throw new IllegalStateException("Doctor is already booked at this time");
        }
        appointment.setStatus(AppointmentStatus.BOOKED);
        return appointmentRepository.save(appointment);
    }

    public List<Appointment> getAppointmentsForDoctorOnDate(Long doctorId, LocalDate date) {
        // Real implementation would query by doctor and date
        Doctor doctor = doctorRepository.findById(doctorId).orElse(null);
        if (doctor == null) return List.of();
        return appointmentRepository.findAll().stream()
                .filter(a -> a.getDoctor().getId().equals(doctorId)
                        && a.getAppointmentTime().toLocalDate().equals(date))
                .toList();
    }
}
