package co.unicauca.piedrazul.patient.presentation.controller;

import co.unicauca.piedrazul.patient.domain.entities.Patient;
import co.unicauca.piedrazul.patient.domain.service.PatientService;
import co.unicauca.piedrazul.patient.presentation.dto.PatientDTOs;
import co.unicauca.piedrazul.patient.presentation.dto.PatientDTOs.*;
import co.unicauca.piedrazul.patient.presentation.mapper.PatientMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@RestController
@RequestMapping("/api/v1/patients")
@Tag(name = "Patients", description = "Gestión de pacientes")
public class PatientController {

    private final PatientService patientService;
    private final PatientMapper patientMapper;
    private final RestTemplate restTemplate;

    @Value("${identity.service.url}")
    private String identityServiceUrl;

    public PatientController(PatientService patientService, PatientMapper patientMapper, RestTemplate restTemplate) {
        this.patientService = patientService;
        this.patientMapper = patientMapper;
        this.restTemplate = restTemplate;
    }

    // Público — el paciente aún no tiene cuenta cuando se registra por web
    /**
     * Perfil del paciente autenticado.
     * Tres intentos de resolución, en orden de preferencia:
     *   1. Claim "userId" del JWT (puede llegar como Number, String o List según el mapper de Keycloak)
     *   2. Claim "email"
     *   3. Claim "preferred_username" (puede ser el número de documento o el correo)
     */
    @GetMapping("/me")
    @Operation(summary = "Obtener perfil del paciente autenticado")
    @PreAuthorize("hasRole('PACIENTE')")
    public ResponseEntity<?> getMyProfile(@AuthenticationPrincipal Jwt jwt) {
        // Intento 1: claim userId — el mapper de Keycloak puede enviarlo como Number, String o List<String>
        Object userIdClaim = jwt.getClaim("userId");
        Long documentId = null;
        if (userIdClaim instanceof Number num) {
            documentId = num.longValue();
        } else if (userIdClaim instanceof String s) {
            try { documentId = Long.parseLong(s.trim()); } catch (NumberFormatException ignored) {}
        } else if (userIdClaim instanceof java.util.List<?> list && !list.isEmpty()) {
            Object first = list.get(0);
            if (first instanceof String s) {
                try { documentId = Long.parseLong(s.trim()); } catch (NumberFormatException ignored) {}
            } else if (first instanceof Number n) {
                documentId = n.longValue();
            }
        }
        if (documentId != null && documentId > 0) {
            try {
                return ResponseEntity.ok(patientMapper.toResponse(patientService.findById(documentId)));
            } catch (IllegalArgumentException ignored) { /* continuar con siguiente intento */ }
        }

        // Intento 2: fallback por email del token
        String email = jwt.getClaimAsString("email");
        if (email != null && !email.isBlank()) {
            try {
                return ResponseEntity.ok(patientMapper.toResponse(patientService.findByEmail(email)));
            } catch (IllegalArgumentException ignored) { /* continuar */ }
        }

        // Intento 3: preferred_username — presente siempre en JWT de Keycloak
        // Puede ser el número de documento (numérico) o el correo del paciente
        String preferredUsername = jwt.getClaimAsString("preferred_username");
        if (preferredUsername != null && !preferredUsername.isBlank()) {
            try {
                long docId = Long.parseLong(preferredUsername.trim());
                if (docId > 0) {
                    return ResponseEntity.ok(patientMapper.toResponse(patientService.findById(docId)));
                }
            } catch (NumberFormatException ignored) {}
            // Si no es un número, intentar como email
            try {
                return ResponseEntity.ok(patientMapper.toResponse(patientService.findByEmail(preferredUsername)));
            } catch (IllegalArgumentException ignored) {}
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
               .body(new MessageResponse("No se pudo identificar al paciente"));
    }

    @PostMapping("/register/web")
    @Operation(summary = "Registro web")
    public ResponseEntity<?> registerFromWeb(@Valid @RequestBody WebRegisterRequest request) {
        try {
            Patient patient = patientMapper.toEntity(request);
            Patient saved = patientService.registerFromWeb(patient);
            // Solo crear cuenta Keycloak si el paciente proporcionó correo y contraseña
            boolean hasEmail    = patient.getEmail()    != null && !patient.getEmail().isBlank();
            boolean hasPassword = patient.getPassword() != null && !patient.getPassword().isBlank();
            if (hasEmail && hasPassword) {
                patient.setUsername(patient.getEmail()); // username = correo
                try {
                    PatientDTOs.PatientRegisterRequest identityRequest = patientMapper.toRegister(patient);
                    restTemplate.postForObject(
                            identityServiceUrl + "/api/v1/identity/register/patient",
                            identityRequest,
                            Void.class
                    );
                } catch (Exception ignored) {
                    // Si identity-service falla (ej: usuario ya existe), el paciente ya fue guardado
                }
            }
            return ResponseEntity.status(HttpStatus.CREATED).body(patientMapper.toResponse(saved));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new MessageResponse(e.getMessage()));
        }
    }

    @PostMapping("/register/agendador")
    @Operation(summary = "Registro por agendador")
    @PreAuthorize("hasRole('AGENDADOR')")
    public ResponseEntity<?> registerFromAgendador(@Valid @RequestBody AgendadorRegisterRequest request) {
        try {
            Patient patient = patientMapper.toEntity(request);
            Patient saved = patientService.registerFromAgendador(patient);
            if (patient.getEmail() != null && !patient.getEmail().isBlank()) {
                patient.setUsername(patient.getEmail());
                patient.setPassword(String.valueOf(patient.getId()));
                PatientDTOs.PatientRegisterRequest identityRequest = patientMapper.toRegister(patient);
                restTemplate.postForObject(
                        identityServiceUrl + "/api/v1/identity/register/patient",
                        identityRequest,
                        Void.class
                );
            }
            return ResponseEntity.status(HttpStatus.CREATED).body(patientMapper.toResponse(saved));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new MessageResponse(e.getMessage()));
        }
    }

    @GetMapping
    @Operation(summary = "Listar pacientes")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENDADOR')")
    public ResponseEntity<List<PatientResponse>> listAll() {
        return ResponseEntity.ok(
                patientService.listAll().stream().map(patientMapper::toResponse).toList());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar paciente por documento")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENDADOR', 'DOCTOR')")
    public ResponseEntity<?> findById(@PathVariable long id) {
        try {
            return ResponseEntity.ok(patientMapper.toResponse(patientService.findById(id)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse(e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar paciente")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENDADOR')")
    public ResponseEntity<?> update(@PathVariable long id, @Valid @RequestBody AgendadorRegisterRequest request) {
        try {
            Patient patient = patientMapper.toEntity(request);
            patient.setId(id);
            return ResponseEntity.ok(patientMapper.toResponse(patientService.update(patient)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse(e.getMessage()));
        }
    }
}
