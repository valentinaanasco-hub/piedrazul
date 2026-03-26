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
    @DisplayName("Registrar paciente nuevo -> guarda y retorna true")
    void registerPatient_pacienteNuevo_guardaYRetornaTrue() {
        Patient patient = buildPatient(1, "agomez");

        // findById retorna null -> paciente no existe -> el validador acepta null
        when(patientRepository.findById(1)).thenReturn(null);
        doNothing().when(validator).validateExists(null);
        when(patientRepository.save(patient)).thenReturn(true);

        boolean result = patientService.registerPatient(patient);

        assertTrue(result);
        verify(patientRepository).save(patient);
    }

    @Test
    @DisplayName("Registrar paciente duplicado -> validador lanza excepción")
    void registerPatient_pacienteDuplicado_lanzaExcepcion() {
        Patient existing = buildPatient(1, "agomez");

        when(patientRepository.findById(1)).thenReturn(existing);
        doThrow(new IllegalArgumentException("El paciente ya existe"))
                .when(validator).validateExists(existing);

        Patient newPatient = buildPatient(1, "agomez");
        assertThrows(IllegalArgumentException.class, () -> patientService.registerPatient(newPatient));
        verify(patientRepository, never()).save(any());
    }

    // -----------------------------------------------------------------------
    // listPatients
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Listar pacientes -> retorna lista del repositorio")
    void listPatients_retornaListaDelRepositorio() {
        List<Patient> patients = List.of(buildPatient(1, "u1"), buildPatient(2, "u2"));
        when(patientRepository.findAll()).thenReturn(patients);

        List<Patient> result = patientService.listPatients();

        assertEquals(2, result.size());
    }

    // -----------------------------------------------------------------------
    // modifyPatient
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Modificar paciente existente -> actualiza y retorna true")
    void modifyPatient_pacienteExistente_actualizaYRetornaTrue() {
        Patient patient = buildPatient(1, "agomez");
        when(patientRepository.findById(1)).thenReturn(patient);
        when(patientRepository.update(patient)).thenReturn(true);

        boolean result = patientService.modifyPatient(patient);

        assertTrue(result);
        verify(patientRepository).update(patient);
    }

    // -----------------------------------------------------------------------
    // deactivatePatient
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Desactivar paciente existente -> retorna true")
    void deactivatePatient_pacienteExistente_retornaTrue() {
        Patient patient = buildPatient(1, "agomez");
        when(patientRepository.findById(1)).thenReturn(patient);
        when(patientRepository.deactivate(1)).thenReturn(true);

        boolean result = patientService.deactivatePatient(1);

        assertTrue(result);
        verify(patientRepository).deactivate(1);
    }

    @Test
    @DisplayName("Desactivar paciente inexistente -> validador lanza excepción")
    void deactivatePatient_pacienteInexistente_lanzaExcepcion() {
        when(patientRepository.findById(99)).thenReturn(null);
        doThrow(new IllegalArgumentException("Paciente no encontrado"))
                .when(validator).validateExists(null);

        assertThrows(IllegalArgumentException.class, () -> patientService.deactivatePatient(99));
        verify(patientRepository, never()).deactivate(anyInt());
    }
}