package co.unicauca.piedrazul.domain.services;

import co.unicauca.piedrazul.domain.access.IPatientRepository;
import co.unicauca.piedrazul.domain.entities.Patient;
import co.unicauca.piedrazul.domain.entities.enums.UserState;
import co.unicauca.piedrazul.domain.services.interfaces.IPatientValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para PatientService.
 *
 * NOTA IMPORTANTE: registerPatient() en el servicio real solo llama
 * validator.validatePatient(patient) y luego patientRepository.save(patient).
 * NO verifica duplicados internamente — eso lo hace el validador externo.
 * Los tests reflejan el flujo real del código.
 */
@ExtendWith(MockitoExtension.class)
class PatientServiceTest {

    @Mock
    private IPatientRepository patientRepository;

    @Mock
    private IPatientValidator validator;

    private PatientService patientService;

    @BeforeEach
    void setUp() {
        patientService = new PatientService(patientRepository, validator);
    }

    // -----------------------------------------------------------------------
    // Helper
    // -----------------------------------------------------------------------

    private Patient buildPatient(int id, String username) {
        Patient p = new Patient();
        p.setId(id);
        p.setUsername(username);
        p.setFirstName("Ana");
        p.setFirstSurname("Gomez");
        p.setPhone("3001234567");
        p.setEmail("ana@mail.com");
        p.setState(UserState.ACTIVO);
        return p;
    }

    // -----------------------------------------------------------------------
    // registerPatient
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("registerPatient: datos válidos -> valida y guarda correctamente")
    void registerPatient_datosValidos_guardaYRetornaTrue() {
        // GIVEN: el validador acepta los datos (no lanza excepción)
        Patient patient = buildPatient(1, "agomez");
        when(patientRepository.save(patient)).thenReturn(true);

        // WHEN
        boolean result = patientService.registerPatient(patient);

        // THEN
        assertTrue(result);
        verify(validator).validatePatient(patient); // Se valida antes de guardar
        verify(patientRepository).save(patient);
    }

    @Test
    @DisplayName("registerPatient: datos inválidos -> validator lanza excepción, no guarda")
    void registerPatient_datosInvalidos_lanzaExcepcionYNoGuarda() {
        Patient patient = buildPatient(1, ""); // Username inválido
        doThrow(new IllegalArgumentException("Datos del paciente inválidos"))
                .when(validator).validatePatient(patient);

        assertThrows(IllegalArgumentException.class, () -> patientService.registerPatient(patient));
        verify(patientRepository, never()).save(any());
    }

    // -----------------------------------------------------------------------
    // findPatient
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("findPatient: id existente -> retorna el paciente correcto")
    void findPatient_idExistente_retornaPaciente() {
        Patient patient = buildPatient(1, "agomez");
        when(patientRepository.findById(1)).thenReturn(patient);

        Patient result = patientService.findPatient(1);

        assertNotNull(result);
        assertEquals(1, result.getId());
        verify(validator).validateExists(patient);
    }

    @Test
    @DisplayName("findPatient: id inexistente -> validator lanza excepción")
    void findPatient_idInexistente_lanzaExcepcion() {
        when(patientRepository.findById(99)).thenReturn(null);
        doThrow(new IllegalArgumentException("Paciente no encontrado"))
                .when(validator).validateExists(null);

        assertThrows(IllegalArgumentException.class, () -> patientService.findPatient(99));
    }

    // -----------------------------------------------------------------------
    // listPatients
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("listPatients: retorna la lista completa del repositorio")
    void listPatients_retornaListaCompleta() {
        List<Patient> patients = List.of(
                buildPatient(1, "u1"),
                buildPatient(2, "u2"),
                buildPatient(3, "u3")
        );
        when(patientRepository.findAll()).thenReturn(patients);

        List<Patient> result = patientService.listPatients();

        assertEquals(3, result.size());
        verify(patientRepository).findAll();
    }

    // -----------------------------------------------------------------------
    // modifyPatient
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("modifyPatient: paciente existente con datos válidos -> actualiza correctamente")
    void modifyPatient_pacienteExistente_actualizaYRetornaTrue() {
        Patient patient = buildPatient(1, "agomez");
        when(patientRepository.findById(1)).thenReturn(patient);
        when(patientRepository.update(patient)).thenReturn(true);

        boolean result = patientService.modifyPatient(patient);

        assertTrue(result);
        verify(validator).validatePatient(patient);  // Valida datos nuevos
        verify(validator).validateExists(patient);   // Verifica que existe
        verify(patientRepository).update(patient);
    }

    @Test
    @DisplayName("modifyPatient: paciente inexistente -> validator lanza excepción, no actualiza")
    void modifyPatient_pacienteInexistente_lanzaExcepcion() {
        Patient patient = buildPatient(99, "nadie");
        when(patientRepository.findById(99)).thenReturn(null);
        doThrow(new IllegalArgumentException("Paciente no encontrado"))
                .when(validator).validateExists(null);

        assertThrows(IllegalArgumentException.class, () -> patientService.modifyPatient(patient));
        verify(patientRepository, never()).update(any());
    }

    // -----------------------------------------------------------------------
    // deactivatePatient
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("deactivatePatient: paciente existente -> verifica existencia y desactiva")
    void deactivatePatient_pacienteExistente_verificaYDesactiva() {
        Patient patient = buildPatient(1, "agomez");
        when(patientRepository.findById(1)).thenReturn(patient);
        when(patientRepository.deactivate(1)).thenReturn(true);

        boolean result = patientService.deactivatePatient(1);

        assertTrue(result);
        verify(validator).validateExists(patient);
        verify(patientRepository).deactivate(1);
    }

    @Test
    @DisplayName("deactivatePatient: paciente inexistente -> validator lanza excepción, no desactiva")
    void deactivatePatient_pacienteInexistente_lanzaExcepcion() {
        when(patientRepository.findById(99)).thenReturn(null);
        doThrow(new IllegalArgumentException("Paciente no encontrado"))
                .when(validator).validateExists(null);

        assertThrows(IllegalArgumentException.class, () -> patientService.deactivatePatient(99));
        verify(patientRepository, never()).deactivate(anyInt());
    }
}