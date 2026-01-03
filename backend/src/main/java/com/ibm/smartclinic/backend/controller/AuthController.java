package com.ibm.smartclinic.backend.controller;

import com.ibm.smartclinic.backend.dto.AuthRequestDto;
import com.ibm.smartclinic.backend.dto.AuthResponseDto;
import com.ibm.smartclinic.backend.dto.DoctorRegisterRequestDto;
import com.ibm.smartclinic.backend.dto.DoctorResponseDto;
import com.ibm.smartclinic.backend.dto.PatientRegisterRequestDto;
import com.ibm.smartclinic.backend.dto.PatientResponseDto;
import com.ibm.smartclinic.backend.model.Doctor;
import com.ibm.smartclinic.backend.model.Patient;
import com.ibm.smartclinic.backend.model.UserIdentity;
import com.ibm.smartclinic.backend.model.UserRole;
import com.ibm.smartclinic.backend.security.Role;
import com.ibm.smartclinic.backend.security.TokenService;
import com.ibm.smartclinic.backend.service.DoctorService;
import com.ibm.smartclinic.backend.service.IdentityService;
import com.ibm.smartclinic.backend.service.PatientService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final DoctorService doctorService;
    private final PatientService patientService;
    private final IdentityService identityService;
    private final TokenService tokenService;

    public AuthController(@NonNull DoctorService doctorService,
                          @NonNull PatientService patientService,
                          @NonNull IdentityService identityService,
                          @NonNull TokenService tokenService) {
        this.doctorService = doctorService;
        this.patientService = patientService;
        this.identityService = identityService;
        this.tokenService = tokenService;
    }

    @PostMapping("/doctor/register")
    public @NonNull ResponseEntity<DoctorResponseDto> registerDoctor(@NonNull @Valid @RequestBody DoctorRegisterRequestDto requestDto) {
        UserIdentity identity = identityService.registerIdentity(requestDto.getEmail(), requestDto.getPassword(), UserRole.DOCTOR);
        Doctor doctor = new Doctor();
        doctor.setName(requestDto.getName());
        doctor.setEmail(identity.getEmail());
        doctor.setSpeciality(requestDto.getSpeciality());
        doctor.setPassword(requestDto.getPassword());

        Doctor saved = doctorService.saveDoctorWithHashedPassword(doctor);
        DoctorResponseDto responseDto = new DoctorResponseDto(saved.getId(), saved.getName(), saved.getEmail(), saved.getSpeciality());
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @PostMapping("/patient/register")
    public @NonNull ResponseEntity<PatientResponseDto> registerPatient(@NonNull @Valid @RequestBody PatientRegisterRequestDto requestDto) {
        UserIdentity identity = identityService.registerIdentity(requestDto.getEmail(), requestDto.getPassword(), UserRole.PATIENT);
        Patient patient = new Patient();
        patient.setName(requestDto.getName());
        patient.setEmail(identity.getEmail());
        patient.setPhone(requestDto.getPhone());
        patient.setPassword(requestDto.getPassword());

        Patient saved = patientService.savePatientWithHashedPassword(patient);
        PatientResponseDto responseDto = new PatientResponseDto(saved.getId(), saved.getName(), saved.getEmail(), saved.getPhone());
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @PostMapping("/doctor/login")
    public @NonNull ResponseEntity<AuthResponseDto> doctorLogin(@NonNull @Valid @RequestBody AuthRequestDto requestDto) {
        log.info("Doctor login attempt for {}", requestDto.getEmail());

        UserIdentity identity = identityService.authenticate(requestDto.getEmail(), requestDto.getPassword(), UserRole.DOCTOR);

        Doctor doctor = doctorService.findByEmail(identity.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        String token = tokenService.generateToken(identity.getEmail(), Role.DOCTOR.name());
        return ResponseEntity.ok(new AuthResponseDto(token, Role.DOCTOR.name()));
    }

    @PostMapping({"/patient/login", "/auth/patient/login"})
    public @NonNull ResponseEntity<AuthResponseDto> patientLogin(@NonNull @Valid @RequestBody AuthRequestDto requestDto) {
        log.info("Patient login attempt for {}", requestDto.getEmail());

        UserIdentity identity = identityService.authenticate(requestDto.getEmail(), requestDto.getPassword(), UserRole.PATIENT);

        Patient patient = patientService.findByEmail(identity.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        String token = tokenService.generateToken(identity.getEmail(), Role.PATIENT.name());
        return ResponseEntity.ok(new AuthResponseDto(token, Role.PATIENT.name()));
    }
}
