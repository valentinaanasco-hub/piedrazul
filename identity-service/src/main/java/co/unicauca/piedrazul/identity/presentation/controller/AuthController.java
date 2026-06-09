package co.unicauca.piedrazul.identity.presentation.controller;

import co.unicauca.piedrazul.identity.domain.entities.User;
import co.unicauca.piedrazul.identity.domain.enums.UserState;
import co.unicauca.piedrazul.identity.domain.service.IdentityService;
import co.unicauca.piedrazul.identity.presentation.dto.IdentityDTOs.*;
import co.unicauca.piedrazul.identity.presentation.mapper.UserMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/identity")
@Tag(name = "Identity", description = "Autenticación y gestión de usuarios")
public class AuthController {

    private final IdentityService identityService;
    private final UserMapper userMapper;

    public AuthController(IdentityService identityService, UserMapper userMapper) {
        this.identityService = identityService;
        this.userMapper = userMapper;
    }

    // Público — autenticación propia (sin JWT)
    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            User user = identityService.login(request.username(), request.password());
            return ResponseEntity.ok(userMapper.toResponse(user));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new MessageResponse(e.getMessage()));
        }
    }

    // Solo ADMIN y AGENDADOR pueden crear usuarios del sistema
    @PostMapping("/register")
    @Operation(summary = "Registrar usuario")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENDADOR')")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterUserRequest request) {
        try {
            User user = new User();
            user.setId(request.userId());
            user.setUsername(request.username());
            user.setPassword(request.password());
            user.setFirstName(request.firstName());
            user.setMiddleName(request.middleName());
            user.setFirstSurname(request.firstSurname());
            user.setLastName(request.lastName());
            user.setUserTypeId(request.userTypeId());
            user.setState(UserState.ACTIVO);
            User saved = identityService.register(user, request.roleName());
            return ResponseEntity.status(HttpStatus.CREATED).body(userMapper.toResponse(saved));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new MessageResponse(e.getMessage()));
        }
    }


    // Cualquiera puede registrar un paciente
    @PostMapping("/register/patient")
    @Operation(summary = "Registrar usuario")
    public ResponseEntity<?> registerPatient(@Valid @RequestBody RegisterUserRequest request) {
        try {
            User user = new User();
            user.setId(request.userId());
            user.setUsername(request.username());
            user.setPassword(request.password());
            user.setFirstName(request.firstName());
            user.setMiddleName(request.middleName());
            user.setFirstSurname(request.firstSurname());
            user.setLastName(request.lastName());
            user.setUserTypeId(request.userTypeId());
            user.setState(UserState.ACTIVO);
            User saved = identityService.register(user, "PACIENTE");
            return ResponseEntity.status(HttpStatus.CREATED).body(userMapper.toResponse(saved));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new MessageResponse(e.getMessage()));
        }
    }


    @GetMapping("/users")
    @Operation(summary = "Listar usuarios")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENDADOR')")
    public ResponseEntity<List<UserResponse>> listUsers() {
        return ResponseEntity.ok(
                identityService.listAll().stream().map(userMapper::toResponse).toList());
    }

    @GetMapping("/users/{id}")
    @Operation(summary = "Buscar usuario por ID")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENDADOR', 'DOCTOR')")
    public ResponseEntity<?> findById(@PathVariable long id) {
        try {
            return ResponseEntity.ok(userMapper.toResponse(identityService.findById(id)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse(e.getMessage()));
        }
    }

    // Uso interno — llamado desde appointment-service sin JWT (no expuesto en API gateway)
    @GetMapping("/internal/users/{id}")
    @Operation(summary = "Buscar usuario por ID (uso interno entre servicios)")
    public ResponseEntity<?> findByIdInternal(@PathVariable long id) {
        try {
            return ResponseEntity.ok(userMapper.toResponse(identityService.findById(id)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse(e.getMessage()));
        }
    }

    @GetMapping("/users/by-username/{username}")
    @Operation(summary = "Buscar usuario por username")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENDADOR')")
    public ResponseEntity<?> findByUsername(@PathVariable String username) {
        try {
            return ResponseEntity.ok(userMapper.toResponse(identityService.findByUsername(username)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse(e.getMessage()));
        }
    }

    @PatchMapping("/users/{id}/deactivate")
    @Operation(summary = "Desactivar usuario")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deactivate(@PathVariable long id) {
        try {
            identityService.deactivate(id);
            return ResponseEntity.ok(new MessageResponse("Usuario desactivado correctamente"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse(e.getMessage()));
        }
    }
}
