package co.unicauca.piedrazul.patient.presentation.controller;

import co.unicauca.piedrazul.patient.domain.entities.Patient;
import co.unicauca.piedrazul.patient.domain.service.PatientService;
import co.unicauca.piedrazul.patient.presentation.dto.PatientDTOs.*;
import co.unicauca.piedrazul.patient.presentation.mapper.PatientMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para gestión de pacientes.
 *
 * @author Santiago Solarte
 */
@RestController
@RequestMapping("/api/v1/patients")
@Tag(name = "Patients", description = "Gestión de pacientes")
public class PatientController {

    private final PatientService patientService;
    private final PatientMapper patientMapper;

    public PatientController(PatientService patientService, PatientMapper patientMapper) {
        this.patientService = patientService;
        this.patientMapper = patientMapper;
    }

    // --- Registro web (RF3) ---

    @PostMapping("/register/web")
    @Operation(summary = "Registro web", description = "El paciente se registra desde la web — todos los campos obligatorios")
    public ResponseEntity<?> registerFromWeb(@Valid @RequestBody WebRegisterRequest request) {
        try {
            Patient patient = patientMapper.toEntity(request);
            Patient saved = patientService.registerFromWeb(patient);
            return ResponseEntity.status(HttpStatus.CREATED).body(patientMapper.toResponse(saved));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new MessageResponse(e.getMessage()));
        }
    }

    // --- Registro por agendador (RF2) ---

    @PostMapping("/register/agendador")
    @Operation(summary = "Registro por agendador", description = "El agendador registra al paciente — fecha y correo opcionales")
    public ResponseEntity<?> registerFromAgendador(@Valid @RequestBody AgendadorRegisterRequest request) {
        try {
            Patient patient = patientMapper.toEntity(request);
            Patient saved = patientService.registerFromAgendador(patient);
            return ResponseEntity.status(HttpStatus.CREATED).body(patientMapper.toResponse(saved));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new MessageResponse(e.getMessage()));
        }
    }

    // --- Consultas ---

    @GetMapping
    @Operation(summary = "Listar pacientes", description = "Obtiene todos los pacientes del sistema")
    public ResponseEntity<List<PatientResponse>> listAll() {
        List<PatientResponse> patients = patientService.listAll().stream()
                .map(patientMapper::toResponse)
                .toList();
        return ResponseEntity.ok(patients);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar paciente por documento")
    public ResponseEntity<?> findById(@PathVariable int id) {
        try {
            return ResponseEntity.ok(patientMapper.toResponse(patientService.findById(id)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse(e.getMessage()));
        }
    }

    // --- Actualización ---

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar paciente")
    public ResponseEntity<?> update(@PathVariable int id, @Valid @RequestBody AgendadorRegisterRequest request) {
        try {
            Patient patient = patientMapper.toEntity(request);
            patient.setId(id);
            return ResponseEntity.ok(patientMapper.toResponse(patientService.update(patient)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse(e.getMessage()));
        }
    }
}
