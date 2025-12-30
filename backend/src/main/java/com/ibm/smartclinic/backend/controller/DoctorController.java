import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
package com.ibm.smartclinic.backend.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import com.ibm.smartclinic.backend.model.Doctor;
import com.ibm.smartclinic.backend.dto.DoctorResponseDto;
import com.ibm.smartclinic.backend.service.DoctorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;

@RestController
@RequestMapping("/api/doctors")
public class DoctorController {

    private final DoctorService doctorService;

    public DoctorController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }

    
    @GetMapping
    @Operation(summary = "Get paginated list of doctors", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "List of doctors returned"),
        @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<Page<DoctorResponseDto>> getAllDoctors(
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        Page<Doctor> page = doctorService.getAllDoctors(pageable);
        Page<DoctorResponseDto> dtoPage = page.map(this::toDoctorResponseDto);
        return ResponseEntity.ok(dtoPage);
    }

    @GetMapping("/{doctorId}/availability")
        @Operation(summary = "Get available time slots for a doctor on a given date", security = @SecurityRequirement(name = "bearerAuth"))
        @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of available time slots returned"),
            @ApiResponse(responseCode = "400", description = "Invalid parameters"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Doctor not found")
        })
        public ResponseEntity<List<String>> getDoctorAvailability(
                @PathVariable @Min(value = 1, message = "Doctor ID must be positive") Long doctorId,
                @RequestParam @NotBlank(message = "Date parameter is required") String date) {
            // Returns available time slots as strings (e.g., "10:00")
            return ResponseEntity.ok(
                doctorService.getAvailableTimeSlots(
                    doctorId,
                    LocalDate.parse(date)
                ).stream().map(Object::toString).toList()
            );
        }

    @GetMapping("/search")
        @Operation(summary = "Search doctors by speciality", security = @SecurityRequirement(name = "bearerAuth"))
        @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of doctors returned"),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
        })
        public ResponseEntity<Page<DoctorResponseDto>> searchDoctors(
                @RequestParam @NotBlank(message = "Speciality parameter is required") String speciality,
                @RequestParam(required = false) String time,
                @PageableDefault(size = 20, sort = "name") Pageable pageable) {
            Page<Doctor> page = doctorService.findBySpeciality(speciality, pageable);
            Page<DoctorResponseDto> dtoPage = page.map(this::toDoctorResponseDto);
            return ResponseEntity.ok(dtoPage);
        }

        private DoctorResponseDto toDoctorResponseDto(Doctor doctor) {
        return new DoctorResponseDto(
            doctor.getId(),
            doctor.getName(),
            doctor.getEmail(),
            doctor.getSpeciality()
        );
        }

}
