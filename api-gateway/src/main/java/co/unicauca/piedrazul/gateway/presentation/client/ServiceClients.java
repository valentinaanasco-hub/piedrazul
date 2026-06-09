package co.unicauca.piedrazul.gateway.presentation.client;

import co.unicauca.piedrazul.gateway.presentation.dto.GatewayDTOs.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

/**
 * Clientes REST para comunicación con los microservicios.
 *
 * @author Santiago Solarte
 */
@Component
public class ServiceClients {

    private final RestTemplate restTemplate;

    @Value("${services.identity.url}")
    private String identityUrl;

    @Value("${services.patient.url}")
    private String patientUrl;

    public ServiceClients(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Registra el usuario en identity-service.
     */
    public ResponseEntity<Object> registerUser(IdentityRegisterRequest request) {
        try {
            return restTemplate.postForEntity(
                    identityUrl + "/api/v1/identity/register",
                    request,
                    Object.class
            );
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body("Identity service no disponible: " + e.getMessage());
        }
    }

    /**
     * Registra los datos del paciente en patient-service.
     */
    public ResponseEntity<Object> registerPatient(PatientServiceRequest request) {
        try {
            return restTemplate.postForEntity(
                    patientUrl + "/api/v1/patients/register/web",
                    request,
                    Object.class
            );
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body("Patient service no disponible: " + e.getMessage());
        }
    }

    /**
     * Rollback — desactiva el usuario en identity-service si el registro del paciente falló.
     */
    public void deactivateUser(long userId) {
        try {
            restTemplate.patchForObject(
                    identityUrl + "/api/v1/identity/users/" + userId + "/deactivate",
                    null,
                    Object.class
            );
        } catch (Exception e) {
            System.err.println("[SAGA] Error al desactivar usuario " + userId + ": " + e.getMessage());
        }
    }
}