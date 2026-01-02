package com.ibm.smartclinic.backend.controller;

import com.ibm.smartclinic.backend.dto.AuthRequestDto;
import com.ibm.smartclinic.backend.dto.AuthResponseDto;
import com.ibm.smartclinic.backend.dto.DoctorRegisterRequestDto;
import com.ibm.smartclinic.backend.dto.DoctorResponseDto;
import com.ibm.smartclinic.backend.dto.PatientRegisterRequestDto;
import com.ibm.smartclinic.backend.dto.PatientResponseDto;
import com.ibm.smartclinic.backend.exception.ResourceNotFoundException;
import com.ibm.smartclinic.backend.exception.ValidationException;
import com.ibm.smartclinic.backend.model.Doctor;
import com.ibm.smartclinic.backend.model.Patient;
import com.ibm.smartclinic.backend.security.Role;
import com.ibm.smartclinic.backend.security.TokenService;
import com.ibm.smartclinic.backend.service.DoctorService;
import com.ibm.smartclinic.backend.service.PatientService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final DoctorService doctorService;
    private final PatientService patientService;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;

    public AuthController(@NonNull DoctorService doctorService,
                          @NonNull PatientService patientService,
                          @NonNull TokenService tokenService,
                          @NonNull PasswordEncoder passwordEncoder) {
        this.doctorService = doctorService;
        this.patientService = patientService;
        this.tokenService = tokenService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/doctor/register")
    public @NonNull ResponseEntity<DoctorResponseDto> registerDoctor(@NonNull @Valid @RequestBody DoctorRegisterRequestDto requestDto) {
        boolean emailTaken = doctorService.findByEmail(requestDto.getEmail()).isPresent();
        if (emailTaken) {
            throw new ValidationException("Doctor email is already registered");
        }

        Doctor doctor = new Doctor();
        doctor.setName(requestDto.getName());
        doctor.setEmail(requestDto.getEmail());
        doctor.setSpeciality(requestDto.getSpeciality());
        doctor.setPassword(requestDto.getPassword());

        Doctor saved = doctorService.saveDoctorWithHashedPassword(doctor);
        DoctorResponseDto responseDto = new DoctorResponseDto(saved.getId(), saved.getName(), saved.getEmail(), saved.getSpeciality());
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @PostMapping("/patient/register")
    public @NonNull ResponseEntity<PatientResponseDto> registerPatient(@NonNull @Valid @RequestBody PatientRegisterRequestDto requestDto) {
        boolean emailTaken = patientService.findByEmail(requestDto.getEmail()).isPresent();
        if (emailTaken) {
            throw new ValidationException("Patient email is already registered");
        }

        Patient patient = new Patient();
        patient.setName(requestDto.getName());
        patient.setEmail(requestDto.getEmail());
        patient.setPhone(requestDto.getPhone());
        patient.setPassword(requestDto.getPassword());

        Patient saved = patientService.savePatientWithHashedPassword(patient);
        PatientResponseDto responseDto = new PatientResponseDto(saved.getId(), saved.getName(), saved.getEmail(), saved.getPhone());
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @PostMapping("/doctor/login")
    public @NonNull ResponseEntity<AuthResponseDto> doctorLogin(@NonNull @Valid @RequestBody AuthRequestDto requestDto) {
        Doctor doctor = doctorService.findByEmail(requestDto.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", "email", requestDto.getEmail()));

        if (!passwordEncoder.matches(requestDto.getPassword(), doctor.getPassword())) {
            throw new ValidationException("Invalid credentials");
        }

        String token = tokenService.generateToken(doctor.getEmail(), Role.DOCTOR.name());
        return ResponseEntity.ok(new AuthResponseDto(token, Role.DOCTOR.name()));
    }

    @PostMapping("/patient/login")
    public @NonNull ResponseEntity<AuthResponseDto> patientLogin(@NonNull @Valid @RequestBody AuthRequestDto requestDto) {
        Patient patient = patientService.findByEmail(requestDto.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Patient", "email", requestDto.getEmail()));

        if (!passwordEncoder.matches(requestDto.getPassword(), patient.getPassword())) {
            throw new ValidationException("Invalid credentials");
        }

        String token = tokenService.generateToken(patient.getEmail(), Role.PATIENT.name());
        return ResponseEntity.ok(new AuthResponseDto(token, Role.PATIENT.name()));
    }
}
