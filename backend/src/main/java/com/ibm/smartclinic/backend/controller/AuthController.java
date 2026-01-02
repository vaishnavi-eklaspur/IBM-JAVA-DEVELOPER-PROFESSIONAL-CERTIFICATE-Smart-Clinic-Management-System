package com.ibm.smartclinic.backend.controller;

import com.ibm.smartclinic.backend.dto.AuthRequestDto;
import com.ibm.smartclinic.backend.dto.AuthResponseDto;
import com.ibm.smartclinic.backend.dto.DoctorRegisterRequestDto;
import com.ibm.smartclinic.backend.dto.DoctorResponseDto;
import com.ibm.smartclinic.backend.dto.PatientRegisterRequestDto;
import com.ibm.smartclinic.backend.dto.PatientResponseDto;
import com.ibm.smartclinic.backend.exception.ApiError;
import com.ibm.smartclinic.backend.exception.ResourceNotFoundException;
import com.ibm.smartclinic.backend.exception.ValidationException;
import com.ibm.smartclinic.backend.model.Doctor;
import com.ibm.smartclinic.backend.model.Patient;
import com.ibm.smartclinic.backend.security.Role;
import com.ibm.smartclinic.backend.security.TokenService;
import com.ibm.smartclinic.backend.service.DoctorService;
import com.ibm.smartclinic.backend.service.PatientService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
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
        String email = normalize(requestDto.getEmail());
        log.info("Doctor login attempt for {}", email);

        Doctor doctor = doctorService.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        if (!passwordMatches(requestDto.getPassword(), doctor.getPassword())) {
            throw new BadCredentialsException("Invalid credentials");
        }

        upgradeDoctorPasswordIfNeeded(doctor, requestDto.getPassword());

        String token = tokenService.generateToken(doctor.getEmail(), Role.DOCTOR.name());
        return ResponseEntity.ok(new AuthResponseDto(token, Role.DOCTOR.name()));
    }

    @PostMapping({"/patient/login", "/auth/patient/login"})
    public @NonNull ResponseEntity<?> patientLogin(@NonNull @Valid @RequestBody AuthRequestDto requestDto) {
        ResponseEntity<ApiError> payloadError = validatePayload(requestDto);
        if (payloadError != null) {
            return payloadError;
        }

        String email = normalize(requestDto.getEmail());
        log.info("Patient login attempt for {}", email);

        Patient patient = patientService.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("Patient", "email", email));

        if (!passwordMatches(requestDto.getPassword(), patient.getPassword())) {
            return badRequest("Invalid credentials");
        }

        upgradePatientPasswordIfNeeded(patient, requestDto.getPassword());

        String token = tokenService.generateToken(patient.getEmail(), Role.PATIENT.name());
        return ResponseEntity.ok(new AuthResponseDto(token, Role.PATIENT.name()));
    }

    private ResponseEntity<ApiError> validatePayload(AuthRequestDto requestDto) {
        String email = requestDto.getEmail();
        String password = requestDto.getPassword();

        if (email == null || email.trim().isEmpty()) {
            return badRequest("Email is required");
        }

        if (password == null || password.isBlank()) {
            return badRequest("Password is required");
        }

        return null;
    }

    private String normalize(String value) {
        return value == null ? null : value.trim();
    }

    private boolean passwordMatches(String rawPassword, String storedPassword) {
        if (storedPassword == null || storedPassword.isBlank()) {
            return false;
        }
        if (passwordEncoder.matches(rawPassword, storedPassword)) {
            return true;
        }
        return !isBcrypt(storedPassword) && storedPassword.equals(rawPassword);
    }

    private void upgradePatientPasswordIfNeeded(Patient patient, String rawPassword) {
        String stored = patient.getPassword();
        if (stored == null || stored.isBlank()) {
            patient.setPassword(passwordEncoder.encode(rawPassword));
            patientService.savePatient(patient);
            return;
        }
        if (!isBcrypt(stored) && stored.equals(rawPassword)) {
            patient.setPassword(passwordEncoder.encode(rawPassword));
            patientService.savePatient(patient);
        }
    }

    private void upgradeDoctorPasswordIfNeeded(Doctor doctor, String rawPassword) {
        String stored = doctor.getPassword();
        if (stored == null || stored.isBlank()) {
            doctor.setPassword(passwordEncoder.encode(rawPassword));
            doctorService.saveDoctor(doctor);
            return;
        }
        if (!isBcrypt(stored) && stored.equals(rawPassword)) {
            doctor.setPassword(passwordEncoder.encode(rawPassword));
            doctorService.saveDoctor(doctor);
        }
    }

    private ResponseEntity<ApiError> badRequest(String message) {
        ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST.value(), message, "VALIDATION_FAILED");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiError);
    }

    private boolean isBcrypt(String value) {
        if (value == null) {
            return false;
        }
        return value.startsWith("$2a$") || value.startsWith("$2b$") || value.startsWith("$2y$");
    }
}
