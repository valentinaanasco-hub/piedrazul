package co.unicauca.piedrazul.medical.presentation.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.unicauca.piedrazul.medical.application.DoctorFacade;
import co.unicauca.piedrazul.medical.domain.entities.DoctorSchedule;
import co.unicauca.piedrazul.medical.domain.service.MedicalStaffService;
import co.unicauca.piedrazul.medical.presentation.dto.MedicalDTOs.DoctorFullInfoResponse;
import co.unicauca.piedrazul.medical.presentation.dto.MedicalDTOs.DoctorResponse;
import co.unicauca.piedrazul.medical.presentation.dto.MedicalDTOs.MessageResponse;
import co.unicauca.piedrazul.medical.presentation.dto.MedicalDTOs.ScheduleUpdateRequest;
import co.unicauca.piedrazul.medical.presentation.mapper.MedicalMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/medical")
@Tag(name = "Medical Staff", description = "Gestión de médicos, horarios y disponibilidad")
public class MedicalStaffController {

    private final MedicalStaffService medicalStaffService;
    private final MedicalMapper medicalMapper;
    private final DoctorFacade doctorFacade;

    public MedicalStaffController(MedicalStaffService medicalStaffService,
                                   MedicalMapper medicalMapper,
                                   DoctorFacade doctorFacade) {
        this.medicalStaffService = medicalStaffService;
        this.medicalMapper = medicalMapper;
        this.doctorFacade = doctorFacade;
    }

    @GetMapping("/doctors")
    @Operation(summary = "Listar todos los médicos")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<DoctorResponse>> listDoctors() {
        return ResponseEntity.ok(
                medicalStaffService.listAllDoctors().stream()
                        .map(medicalMapper::toResponse)
                        .toList()
        );
    }

    @GetMapping("/doctors/{id}")
    @Operation(summary = "Obtener médico por ID")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getDoctorById(@PathVariable int id) {
        try {
            return ResponseEntity.ok(medicalMapper.toResponse(medicalStaffService.findDoctorById(id)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/doctors/specialty/{specialtyId}")
    @Operation(summary = "Listar médicos por especialidad")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<DoctorResponse>> listDoctorsBySpecialty(@PathVariable int specialtyId) {
        return ResponseEntity.ok(
                medicalStaffService.listDoctorsBySpecialty(specialtyId).stream()
                        .map(medicalMapper::toResponse)
                        .toList()
        );
    }

    @GetMapping("/doctors/{id}/schedule")
    @Operation(summary = "Obtener horario de un médico")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENDADOR', 'DOCTOR', 'PACIENTE')")
    public ResponseEntity<?> getDoctorSchedule(@PathVariable int id) {
        try {
            return ResponseEntity.ok(
                    medicalStaffService.getDoctorSchedule(id).stream()
                            .map(medicalMapper::toResponse)
                            .toList()
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/doctors/{id}/schedule")
    @Operation(summary = "Actualizar horario de un médico")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateSchedule(
            @PathVariable int id,
            @Valid @RequestBody ScheduleUpdateRequest request) {
        try {
            List<DoctorSchedule> schedules = medicalMapper.toEntities(request);
            List<DoctorSchedule> saved = medicalStaffService.updateSchedule(id, schedules);
            return ResponseEntity.ok(saved.stream().map(medicalMapper::toResponse).toList());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @GetMapping("/availability")
    @Operation(summary = "Obtener disponibilidad de un médico")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENDADOR', 'PACIENTE', 'DOCTOR')")
    public ResponseEntity<?> getAvailability(
            @RequestParam int doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            return ResponseEntity.ok(medicalStaffService.getAvailability(doctorId, date));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @GetMapping("/doctors/{id}/full-info")
    @Operation(summary = "Información completa del médico")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENDADOR')")
    public ResponseEntity<?> getDoctorFullInfo(
            @PathVariable int id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            DoctorFullInfoResponse info = doctorFacade.getDoctorFullInfo(id, date);
            return ResponseEntity.ok(info);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }
}
