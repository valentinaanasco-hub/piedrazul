package co.unicauca.piedrazul.gateway;

import co.unicauca.piedrazul.gateway.presentation.client.ServiceClients;
import co.unicauca.piedrazul.gateway.presentation.dto.GatewayDTOs.*;
import co.unicauca.piedrazul.gateway.saga.PatientRegistrationSaga;
import co.unicauca.piedrazul.gateway.saga.SagaException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para PatientRegistrationSaga.
 * Verifica el flujo completo y los escenarios de compensación.
 *
 * @author Santiago Solarte
 */
@ExtendWith(MockitoExtension.class)
class PatientRegistrationSagaTest {

    @Mock
    private ServiceClients serviceClients;

    private PatientRegistrationSaga saga;

    private PatientRegisterRequest validRequest;

    @BeforeEach
    void setUp() {
        saga = new PatientRegistrationSaga(serviceClients);

        validRequest = new PatientRegisterRequest(
                12345678, "CC",
                "Juan", "", "Pérez", "",
                "juan@correo.com", "pass1234",
                "3001234567", "Hombre",
                "15", "6", "1990"
        );
    }

    // --- Flujo exitoso ---

    @Test
    void execute_ambosServiciosExitosos_retornaResponse() {
        when(serviceClients.registerUser(any())).thenReturn(ResponseEntity.status(HttpStatus.CREATED).build());
        when(serviceClients.registerPatient(any())).thenReturn(ResponseEntity.status(HttpStatus.CREATED).build());

        RegisterResponse result = saga.execute(validRequest);

        assertNotNull(result);
        assertEquals("Registro exitoso", result.message());
        assertEquals(12345678, result.userId());
        verify(serviceClients, never()).deactivateUser(anyLong());
    }

    // --- Fallo en paso 1 ---

    @Test
    void execute_identityServiceFalla_lanzaSagaExceptionSinCompensacion() {
        when(serviceClients.registerUser(any())).thenReturn(ResponseEntity.status(HttpStatus.CONFLICT).build());

        assertThrows(SagaException.class, () -> saga.execute(validRequest));

        // No debe compensar porque el paso 1 falló — no hay nada que revertir
        verify(serviceClients, never()).deactivateUser(anyLong());
        verify(serviceClients, never()).registerPatient(any());
    }

    // --- Fallo en paso 2 con compensación ---

    @Test
    void execute_patientServiceFalla_ejecutaCompensacion() {
        when(serviceClients.registerUser(any())).thenReturn(ResponseEntity.status(HttpStatus.CREATED).build());
        when(serviceClients.registerPatient(any())).thenReturn(ResponseEntity.status(HttpStatus.CONFLICT).build());

        assertThrows(SagaException.class, () -> saga.execute(validRequest));

        // Debe compensar desactivando el usuario creado en paso 1
        verify(serviceClients, times(1)).deactivateUser(12345678);
    }

    // --- Fallo en paso 2 con excepción inesperada ---

    @Test
    void execute_patientServiceLanzaExcepcion_ejecutaCompensacion() {
        when(serviceClients.registerUser(any())).thenReturn(ResponseEntity.status(HttpStatus.CREATED).build());
        when(serviceClients.registerPatient(any())).thenThrow(new RuntimeException("Servicio no disponible"));

        assertThrows(SagaException.class, () -> saga.execute(validRequest));

        verify(serviceClients, times(1)).deactivateUser(12345678);
    }

    // --- Compensación falla también ---

    @Test
    void execute_compensacionFalla_noLanzaExcepcionAdicional() {
        when(serviceClients.registerUser(any())).thenReturn(ResponseEntity.status(HttpStatus.CREATED).build());
        when(serviceClients.registerPatient(any())).thenReturn(ResponseEntity.status(HttpStatus.CONFLICT).build());
        doThrow(new RuntimeException("Error al desactivar")).when(serviceClients).deactivateUser(anyLong());

        // La saga lanza SagaException pero no una excepción adicional por la compensación fallida
        assertThrows(SagaException.class, () -> saga.execute(validRequest));
    }
}
