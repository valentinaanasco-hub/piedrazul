package co.unicauca.piedrazul.medical.presentation.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
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

/**
 * Controlador REST para gestión del personal médico.
 *
 * @author Ginner Ortega
 */
@RestController
@RequestMapping("/api/v1/medical")
@Tag(name = "Medical Staff", description = "Gestión de médicos, horarios y disponibilidad")
public class MedicalStaffController {

    private final MedicalStaffService medicalStaffService;
    private final MedicalMapper       medicalMapper;
    private final DoctorFacade        doctorFacade;

    public MedicalStaffController(MedicalStaffService medicalStaffService,
                                   MedicalMapper medicalMapper,
                                   DoctorFacade doctorFacade) {
        this.medicalStaffService = medicalStaffService;
        this.medicalMapper       = medicalMapper;
        this.doctorFacade        = doctorFacade;
    }

    // --- Médicos ---

    @GetMapping("/doctors")
    @Operation(summary = "Listar todos los médicos")
    public ResponseEntity<List<DoctorResponse>> listDoctors() {
        return ResponseEntity.ok(
                medicalStaffService.listAllDoctors().stream()
                        .map(medicalMapper::toResponse)
                        .toList()
        );
    }

    @GetMapping("/doctors/{id}")
    @Operation(summary = "Obtener médico por ID")
    public ResponseEntity<?> getDoctorById(@PathVariable int id) {
        try {
            return ResponseEntity.ok(medicalMapper.toResponse(medicalStaffService.findDoctorById(id)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/doctors/specialty/{specialtyId}")
    @Operation(summary = "Listar médicos por especialidad")
    public ResponseEntity<List<DoctorResponse>> listDoctorsBySpecialty(@PathVariable int specialtyId) {
        return ResponseEntity.ok(
                medicalStaffService.listDoctorsBySpecialty(specialtyId).stream()
                        .map(medicalMapper::toResponse)
                        .toList()
        );
    }

    // --- Horarios ---

    @GetMapping("/doctors/{id}/schedule")
    @Operation(summary = "Obtener horario de un médico")
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
    public ResponseEntity<?> updateSchedule(
            @PathVariable int id,
            @Valid @RequestBody ScheduleUpdateRequest request) {
        try {
            List<DoctorSchedule> schedules = medicalMapper.toEntities(request);
            List<DoctorSchedule> saved     = medicalStaffService.updateSchedule(id, schedules);
            return ResponseEntity.ok(
                    saved.stream().map(medicalMapper::toResponse).toList()
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse(e.getMessage()));
        }
    }

    // --- Disponibilidad ---

    @GetMapping("/availability")
    @Operation(
            summary = "Obtener disponibilidad de un médico",
            description = "Retorna las franjas horarias disponibles para un médico en una fecha dada"
    )
    public ResponseEntity<?> getAvailability(
            @RequestParam int doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) List<String> occupied) {
        try {
            List<String> slots = medicalStaffService.getAvailability(
                    doctorId, date, occupied != null ? occupied : List.of()
            );
            return ResponseEntity.ok(slots);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse(e.getMessage()));
        }
    }

    // --- Facade: información completa del médico en una sola llamada ---

    @GetMapping("/doctors/{id}/full-info")
    @Operation(
            summary = "Información completa del médico",
            description = "Retorna en una sola llamada los datos del médico, sus horarios y las franjas disponibles para una fecha. Usa el patrón Facade para simplificar la interfaz del subsistema."
    )
    public ResponseEntity<?> getDoctorFullInfo(
            @PathVariable int id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) List<String> occupied) {
        try {
            DoctorFullInfoResponse info = doctorFacade.getDoctorFullInfo(id, date, occupied);
            return ResponseEntity.ok(info);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse(e.getMessage()));
        }
    }
}
