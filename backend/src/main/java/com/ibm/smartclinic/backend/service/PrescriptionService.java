package com.ibm.smartclinic.backend.service;

import com.ibm.smartclinic.backend.dto.PrescriptionRequestDto;
import com.ibm.smartclinic.backend.dto.PrescriptionResponseDto;
import com.ibm.smartclinic.backend.exception.ResourceNotFoundException;
import com.ibm.smartclinic.backend.exception.ValidationException;
import com.ibm.smartclinic.backend.model.Appointment;
import com.ibm.smartclinic.backend.model.AppointmentStatus;
import com.ibm.smartclinic.backend.model.Doctor;
import com.ibm.smartclinic.backend.model.Prescription;
import com.ibm.smartclinic.backend.repository.AppointmentRepository;
import com.ibm.smartclinic.backend.repository.PrescriptionRepository;
import java.util.List;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;
    private final AppointmentRepository appointmentRepository;

    public PrescriptionService(@NonNull PrescriptionRepository prescriptionRepository,
                               @NonNull AppointmentRepository appointmentRepository) {
        this.prescriptionRepository = prescriptionRepository;
        this.appointmentRepository = appointmentRepository;
    }

    @NonNull
    public PrescriptionResponseDto createPrescription(@NonNull Doctor doctor,
                                                      @NonNull PrescriptionRequestDto requestDto) {
        Appointment appointment = appointmentRepository.findById(requestDto.getAppointmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", "id", requestDto.getAppointmentId()));

        if (appointment.getDoctor() == null || appointment.getPatient() == null) {
            throw new ValidationException("Appointment must have both doctor and patient assigned", "appointmentId");
        }

        if (!appointment.getDoctor().getId().equals(doctor.getId())) {
            throw new ValidationException("Authenticated doctor cannot prescribe for this appointment", "appointmentId");
        }

        if (appointment.getStatus() != AppointmentStatus.COMPLETED) {
            throw new ValidationException("Prescriptions can only be issued after appointment completion", "status");
        }

        String notes = requestDto.getNotes();
        if (notes == null || notes.isBlank()) {
            throw new ValidationException("Prescription notes must not be blank", "notes");
        }

        Prescription prescription = new Prescription();
        prescription.setNotes(notes.trim());
        prescription.setAppointment(appointment);

        Prescription saved = prescriptionRepository.save(prescription);
        return mapToDto(saved);
    }

    @NonNull
    @Transactional(readOnly = true)
    public List<PrescriptionResponseDto> getPrescriptionsForDoctor(@NonNull Long doctorId) {
        return prescriptionRepository.findByAppointmentDoctorId(doctorId)
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    @NonNull
    @Transactional(readOnly = true)
    public List<PrescriptionResponseDto> getPrescriptionsForPatient(@NonNull Long patientId) {
        return prescriptionRepository.findByAppointmentPatientId(patientId)
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    @NonNull
    private PrescriptionResponseDto mapToDto(@NonNull Prescription prescription) {
        Long appointmentId = prescription.getAppointment() != null ? prescription.getAppointment().getId() : null;
        return new PrescriptionResponseDto(prescription.getId(), prescription.getNotes(), appointmentId);
    }
}
