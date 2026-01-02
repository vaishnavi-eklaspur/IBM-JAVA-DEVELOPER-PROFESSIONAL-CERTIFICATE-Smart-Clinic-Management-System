package com.ibm.smartclinic.backend.service;

import com.ibm.smartclinic.backend.exception.ConflictException;
import com.ibm.smartclinic.backend.exception.ResourceNotFoundException;
import com.ibm.smartclinic.backend.exception.ValidationException;
import com.ibm.smartclinic.backend.model.Appointment;
import com.ibm.smartclinic.backend.model.AppointmentStatus;
import com.ibm.smartclinic.backend.model.Doctor;
import com.ibm.smartclinic.backend.repository.AppointmentRepository;
import com.ibm.smartclinic.backend.repository.DoctorRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;

@Service
@Transactional
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;

    public AppointmentService(@NonNull AppointmentRepository appointmentRepository,
                              @NonNull DoctorRepository doctorRepository) {
        this.appointmentRepository = appointmentRepository;
        this.doctorRepository = doctorRepository;
    }

    /**
     * Spring Framework 6 guarantees non-null at runtime; warning is a known static-analysis limitation.
     */
    @NonNull
    public Page<Appointment> getAppointmentsByPatientId(@NonNull Long patientId, @NonNull Pageable pageable) {
        return appointmentRepository.findByPatientId(patientId, pageable);
    }

    @NonNull
    public Appointment bookAppointment(@NonNull Appointment appointment) {
        // Validate input
        if (appointment.getDoctor() == null || appointment.getAppointmentTime() == null) {
            throw new ValidationException("Doctor and appointment time must be provided");
        }
        if (appointment.getPatient() == null || appointment.getPatient().getId() == null) {
            throw new ValidationException("Patient context is required to book an appointment", "patientId");
        }

        // Conflict detection: prevent double-booking same doctor at same time
        boolean conflict = appointmentRepository.findByDoctorAndAppointmentTime(
                appointment.getDoctor(), appointment.getAppointmentTime()
        ).isPresent();
        if (conflict) {
            throw new ConflictException(
                    "Doctor is already booked at this time",
                    "DOUBLE_BOOKING"
            );
        }

        appointment.setStatus(AppointmentStatus.BOOKED);
        return appointmentRepository.save(appointment);
    }

    @NonNull
    public List<Appointment> getAppointmentsForDoctorOnDate(@NonNull Long doctorId, @NonNull LocalDate date) {
        // Real implementation would query by doctor and date
        Doctor doctor = doctorRepository.findById(doctorId).orElse(null);
        if (doctor == null) return List.of();
        return appointmentRepository.findAll().stream()
                .filter(a -> a.getDoctor().getId().equals(doctorId)
                        && a.getAppointmentTime().toLocalDate().equals(date))
                .toList();
    }

    @NonNull
    public List<Appointment> getAppointmentsForDoctor(@NonNull Long doctorId) {
        return appointmentRepository.findByDoctorIdOrderByAppointmentTimeDesc(doctorId);
    }

    @NonNull
    public List<Appointment> getAppointmentsForPatient(@NonNull Long patientId) {
        return appointmentRepository.findByPatientIdOrderByAppointmentTimeDesc(patientId);
    }

    @NonNull
    public Appointment completeAppointment(@NonNull Long appointmentId, @NonNull Doctor doctor) {
        Appointment appointment = requireDoctorOwnedBookedAppointment(appointmentId, doctor);
        appointment.setStatus(AppointmentStatus.COMPLETED);
        return appointmentRepository.save(appointment);
    }

    @NonNull
    public Appointment cancelAppointment(@NonNull Long appointmentId, @NonNull Doctor doctor) {
        Appointment appointment = requireDoctorOwnedBookedAppointment(appointmentId, doctor);
        appointment.setStatus(AppointmentStatus.CANCELLED);
        return appointmentRepository.save(appointment);
    }

    @NonNull
    private Appointment requireDoctorOwnedBookedAppointment(@NonNull Long appointmentId, @NonNull Doctor doctor) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", "id", appointmentId));

        if (appointment.getDoctor() == null || appointment.getDoctor().getId() == null) {
            throw new ValidationException("Appointment is not assigned to a doctor", "appointmentId");
        }
        if (!appointment.getDoctor().getId().equals(doctor.getId())) {
            throw new ValidationException("Doctor cannot modify appointments they do not own", "appointmentId");
        }
        if (appointment.getStatus() != AppointmentStatus.BOOKED) {
            throw new ValidationException("Only booked appointments can be updated", "status");
        }
        return appointment;
    }
}
