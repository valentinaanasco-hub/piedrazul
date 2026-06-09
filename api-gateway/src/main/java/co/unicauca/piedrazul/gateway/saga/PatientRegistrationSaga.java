package co.unicauca.piedrazul.gateway.saga;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import co.unicauca.piedrazul.gateway.presentation.client.ServiceClients;
import co.unicauca.piedrazul.gateway.presentation.dto.GatewayDTOs.IdentityRegisterRequest;
import co.unicauca.piedrazul.gateway.presentation.dto.GatewayDTOs.PatientRegisterRequest;
import co.unicauca.piedrazul.gateway.presentation.dto.GatewayDTOs.PatientServiceRequest;
import co.unicauca.piedrazul.gateway.presentation.dto.GatewayDTOs.RegisterResponse;

/**
 * Implementación del patrón Saga para el registro completo de paciente (RF3).
 *
 * Pasos:
 *   1. Registrar usuario en identity-service  → compensación: desactivar usuario
 *   2. Registrar paciente en patient-service  → si falla, ejecuta compensación del paso 1
 *
 * Este patrón garantiza consistencia eventual entre microservicios
 * sin necesidad de transacciones distribuidas.
 *
 * @author Santiago Solarte
 */
@Component
public class PatientRegistrationSaga {

    private final ServiceClients serviceClients;

    public PatientRegistrationSaga(ServiceClients serviceClients) {
        this.serviceClients = serviceClients;
    }

    /**
     * Ejecuta la saga de registro completo de paciente.
     *
     * @param request Datos completos del paciente desde el frontend.
     * @return RegisterResponse con el resultado del registro.
     * @throws SagaException si algún paso falla.
     */
    public RegisterResponse execute(PatientRegisterRequest request) {
        long userId = request.documentId();

        // --- Paso 1: Registrar en identity-service ---
        IdentityRegisterRequest identityRequest = new IdentityRegisterRequest(
                request.firstName(),
                request.middleName(),
                request.firstSurname(),
                request.lastName(),
                request.email(),
                request.password(),
                request.userTypeId(),
                userId,
                "PACIENTE"
        );

        ResponseEntity<Object> identityResponse = serviceClients.registerUser(identityRequest);

        if (!identityResponse.getStatusCode().equals(HttpStatus.CREATED)) {
            throw new SagaException("Error al registrar credenciales: " + identityResponse.getBody());
        }

        // --- Paso 2: Registrar en patient-service ---
        PatientServiceRequest patientRequest = new PatientServiceRequest(
                request.documentId(),
                request.userTypeId(),
                request.firstName(),
                request.middleName(),
                request.firstSurname(),
                request.lastName(),
                request.email(),
                request.password(),
                request.phone(),
                request.gender(),
                request.birthDay(),
                request.birthMonth(),
                request.birthYear()
        );

        ResponseEntity<Object> patientResponse;

        try {
            patientResponse = serviceClients.registerPatient(patientRequest);
        } catch (Exception e) {
            // --- Compensación ante excepción inesperada en paso 2 ---
            compensate(userId);
            throw new SagaException("Error inesperado en el registro. Registro revertido: " + e.getMessage());
        }

        if (!patientResponse.getStatusCode().equals(HttpStatus.CREATED)) {
            // --- Compensación ante respuesta no exitosa en paso 2 ---
            compensate(userId);
            throw new SagaException("Error al registrar datos del paciente. Registro revertido.");
        }

        // --- Ambos pasos exitosos ---
        return new RegisterResponse(
                "Registro exitoso",
                userId,
                request.firstName() + " " + request.firstSurname(),
                request.email()
        );
    }

    /**
     * Compensación del paso 1 — desactiva el usuario si el paso 2 falló.
     */
    private void compensate(long userId) {
        try {
            serviceClients.deactivateUser(userId);
        } catch (Exception e) {
            System.err.println("[SAGA] Error en compensación para userId=" + userId + ": " + e.getMessage());
        }
    }
}