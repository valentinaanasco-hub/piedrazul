package co.unicauca.piedrazul.gateway.presentation.client;

import co.unicauca.piedrazul.gateway.presentation.dto.GatewayDTOs.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Clientes REST para comunicación con los microservicios.
 *
 * @author Santiago Solarte
 */
@Component
public class ServiceClients {

    private final WebClient webClient;

    @Value("${services.identity.url}")
    private String identityUrl;

    @Value("${services.patient.url}")
    private String patientUrl;

    public ServiceClients(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    /**
     * Registra el usuario en identity-service.
     */
    public ResponseEntity<Object> registerUser(IdentityRegisterRequest request) {
        return webClient.post()
                .uri(identityUrl + "/api/v1/identity/register")
                .bodyValue(request)
                .retrieve()
                .toEntity(Object.class)
                .block();
    }

    /**
     * Registra los datos del paciente en patient-service.
     */
    public ResponseEntity<Object> registerPatient(PatientServiceRequest request) {
        return webClient.post()
                .uri(patientUrl + "/api/v1/patients/register/web")
                .bodyValue(request)
                .retrieve()
                .toEntity(Object.class)
                .block();
    }

    /**
     * Rollback — desactiva el usuario en identity-service si el registro del paciente falló.
     */
    public void deactivateUser(int userId) {
        webClient.patch()
                .uri(identityUrl + "/api/v1/identity/users/" + userId + "/deactivate")
                .retrieve()
                .toBodilessEntity()
                .block();
    }
}