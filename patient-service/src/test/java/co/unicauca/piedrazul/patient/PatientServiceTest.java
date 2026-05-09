package co.unicauca.piedrazul.patient;

import co.unicauca.piedrazul.patient.domain.entities.Patient;
import co.unicauca.piedrazul.patient.domain.enums.UserState;
import co.unicauca.piedrazul.patient.domain.repository.PatientRepository;
import co.unicauca.piedrazul.patient.domain.service.PatientService;
import co.unicauca.piedrazul.patient.domain.strategy.AgendadorPatientValidationStrategy;
import co.unicauca.piedrazul.patient.domain.strategy.WebPatientValidationStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para PatientService.
 * Verifica el patrón Strategy con ambas estrategias de validación.
 *
 * @author Santiago Solarte
 */
@ExtendWith(MockitoExtension.class)
class PatientServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @Spy
    private WebPatientValidationStrategy webValidation;

    @Spy
    private AgendadorPatientValidationStrategy agendadorValidation;

    private PatientService patientService;

    private Patient validWebPatient;
    private Patient validAgendadorPatient;

    @BeforeEach
    void setUp() {
        // Inyección manual
        patientService = new PatientService(patientRepository, webValidation, agendadorValidation);

        // Paciente completo para registro web
        validWebPatient = new Patient();
        validWebPatient.setId(12345678);
        validWebPatient.setFirstName("Juan");
        validWebPatient.setFirstSurname("Pérez");
        validWebPatient.setEmail("juan@correo.com");
        validWebPatient.setUsername("juan@correo.com");
        validWebPatient.setPassword("pass1234");
        validWebPatient.setPhone("3001234567");
        validWebPatient.setGender("Hombre");
        validWebPatient.setUserTypeId("CC");
        validWebPatient.setState(UserState.ACTIVO);

        // Paciente sin correo ni fecha para registro por agendador
        validAgendadorPatient = new Patient();
        validAgendadorPatient.setId(87654321);
        validAgendadorPatient.setFirstName("María");
        validAgendadorPatient.setFirstSurname("García");
        validAgendadorPatient.setPhone("3109876543");
        validAgendadorPatient.setGender("Mujer");
        validAgendadorPatient.setUserTypeId("CC");
        validAgendadorPatient.setState(UserState.ACTIVO);
    }

    // --- Tests registro web ---

    @Test
    void registerFromWeb_datosCompletos_guardaCorrectamente() {
        when(patientRepository.existsById(12345678)).thenReturn(false);
        when(patientRepository.save(any(Patient.class))).thenReturn(validWebPatient);

        Patient result = patientService.registerFromWeb(validWebPatient);

        assertNotNull(result);
        verify(patientRepository, times(1)).save(any(Patient.class));
        verify(webValidation, times(1)).validate(any(Patient.class));
    }

    @Test
    void registerFromWeb_sinEmail_lanzaExcepcion() {
        validWebPatient.setEmail(null);

        assertThrows(IllegalArgumentException.class,
                () -> patientService.registerFromWeb(validWebPatient));
    }

    @Test
    void registerFromWeb_pacienteDuplicado_lanzaExcepcion() {
        when(patientRepository.existsById(12345678)).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> patientService.registerFromWeb(validWebPatient));
    }

    // --- Tests registro agendador ---

    @Test
    void registerFromAgendador_sinEmailNiFecha_guardaCorrectamente() {
        when(patientRepository.existsById(87654321)).thenReturn(false);
        when(patientRepository.save(any(Patient.class))).thenReturn(validAgendadorPatient);

        Patient result = patientService.registerFromAgendador(validAgendadorPatient);

        assertNotNull(result);
        verify(patientRepository, times(1)).save(any(Patient.class));
        verify(agendadorValidation, times(1)).validate(any(Patient.class));
    }

    @Test
    void registerFromAgendador_sinTelefono_lanzaExcepcion() {
        validAgendadorPatient.setPhone(null);

        assertThrows(IllegalArgumentException.class,
                () -> patientService.registerFromAgendador(validAgendadorPatient));
    }

    @Test
    void registerFromAgendador_telefonoInvalido_lanzaExcepcion() {
        validAgendadorPatient.setPhone("123");

        assertThrows(IllegalArgumentException.class,
                () -> patientService.registerFromAgendador(validAgendadorPatient));
    }

    // --- Tests de consulta ---

    @Test
    void findById_idExistente_retornaPaciente() {
        when(patientRepository.findById(12345678)).thenReturn(Optional.of(validWebPatient));

        Patient result = patientService.findById(12345678);

        assertNotNull(result);
        assertEquals(12345678, result.getId());
    }

    @Test
    void findById_idNoExistente_lanzaExcepcion() {
        when(patientRepository.findById(anyInt())).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> patientService.findById(99999));
    }

    @Test
    void listAll_retornaListaCompleta() {
        when(patientRepository.findAll()).thenReturn(List.of(validWebPatient, validAgendadorPatient));

        List<Patient> result = patientService.listAll();

        assertEquals(2, result.size());
    }
}