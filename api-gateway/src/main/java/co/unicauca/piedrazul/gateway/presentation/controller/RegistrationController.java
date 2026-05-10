package co.unicauca.piedrazul.gateway.presentation.controller;

import co.unicauca.piedrazul.gateway.presentation.dto.GatewayDTOs.*;
import co.unicauca.piedrazul.gateway.saga.PatientRegistrationSaga;
import co.unicauca.piedrazul.gateway.saga.SagaException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * Controlador de orquestación para registro de pacientes.
 * Usa el patrón Saga para coordinar identity-service y patient-service.
 * La Saga corre en Schedulers.boundedElastic() para evitar bloquear
 * el hilo reactivo de Netty con llamadas síncronas (RestTemplate).
 *
 * @author Santiago Solarte
 */
@RestController
@RequestMapping("/api/v1/register")
@Tag(name = "Registration", description = "Orquestación de registro completo de paciente")
public class RegistrationController {

    private final PatientRegistrationSaga registrationSaga;

    public RegistrationController(PatientRegistrationSaga registrationSaga) {
        this.registrationSaga = registrationSaga;
    }

    /**
     * Registro completo de paciente desde la web (RF3).
     * Coordina identity-service y patient-service mediante el patrón Saga.
     * Si patient-service falla, se revierte el registro en identity-service.
     */
    @PostMapping("/patient")
    @Operation(
            summary = "Registro completo de paciente",
            description = "Orquesta el registro en identity-service y patient-service usando el patrón Saga"
    )
    public Mono<ResponseEntity<?>> registerPatient(@Valid @RequestBody PatientRegisterRequest request) {
        // Mono.fromCallable + boundedElastic() ejecuta la Saga en un hilo de I/O
        // separado, evitando el error "block() not supported in reactor-http-nio"
        return Mono.<ResponseEntity<?>>fromCallable(() -> {
                    RegisterResponse response = registrationSaga.execute(request);
                    return ResponseEntity.status(HttpStatus.CREATED).body(response);
                })
                .subscribeOn(Schedulers.boundedElastic())
                .onErrorResume(SagaException.class, e ->
                        Mono.just(ResponseEntity.status(HttpStatus.CONFLICT).body(new MessageResponse(e.getMessage())))
                )
                .onErrorResume(Exception.class, e ->
                        Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new MessageResponse("Error inesperado en el servidor")))
                );
    }
}