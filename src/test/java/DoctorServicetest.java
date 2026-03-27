package co.unicauca.piedrazul.domain.services;

import co.unicauca.piedrazul.domain.access.IDoctorRepository;
import co.unicauca.piedrazul.domain.entities.Doctor;
import co.unicauca.piedrazul.domain.services.interfaces.IDoctorValidator;
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
 * Pruebas unitarias para DoctorService.
 *
 * Cubre registro, listado de activos (con filtraje), modificación y desactivación.
 */
@ExtendWith(MockitoExtension.class)
class DoctorServiceTest {

    @Mock
    private IDoctorRepository doctorRepository;

    @Mock
    private IDoctorValidator doctorValidator;

    private DoctorService doctorService;

    @BeforeEach
    void setUp() {
        doctorService = new DoctorService(doctorRepository, doctorValidator);
    }

    // -----------------------------------------------------------------------
    // Helper
    // -----------------------------------------------------------------------

    private Doctor buildDoctor(int id, String username, boolean active) {
        Doctor d = new Doctor();
        d.setId(id);
        d.setUsername(username);
        d.setFirstName("Carlos");
        d.setFirstSurname("Lopez");
        d.setProfessionalId("RM-00" + id);
        // El estado activo/inactivo lo gestiona el repositorio con findAllActive()
        return d;
    }

    // -----------------------------------------------------------------------
    // registerDoctor
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Registrar médico válido -> valida y guarda correctamente")
    void registerDoctor_medicoValido_validaYGuarda() {
        // GIVEN
        Doctor doctor = buildDoctor(1, "clopez", true);
        when(doctorRepository.save(doctor)).thenReturn(true);

        // WHEN
        boolean result = doctorService.registerDoctor(doctor);

        // THEN
        assertTrue(result);
        verify(doctorValidator).validateDoctor(doctor); // Se valida antes de guardar
        verify(doctorRepository).save(doctor);
    }

    @Test
    @DisplayName("Registrar médico con datos inválidos -> validator lanza excepción, no guarda")
    void registerDoctor_medicoInvalido_lanzaExcepcionYNoGuarda() {
        Doctor doctor = buildDoctor(1, "", true); // Username vacío → inválido
        doThrow(new IllegalArgumentException("Datos del médico inválidos"))
                .when(doctorValidator).validateDoctor(doctor);

        assertThrows(IllegalArgumentException.class, () -> doctorService.registerDoctor(doctor));
        verify(doctorRepository, never()).save(any()); // Nunca debe llegar al repositorio
    }

    // -----------------------------------------------------------------------
    // listActiveDoctors — caso clave del enunciado:
    // De 5 doctores, 2 inactivos → el repositorio ya filtra y retorna solo 3 activos
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("listActiveDoctors: repositorio tiene 5 doctores, 2 inactivos -> retorna solo 3 activos")
    void listActiveDoctors_5doctores2inactivos_retorna3Activos() {
        // GIVEN: el repositorio findAllActive() ya hace el filtro en BD
        // Solo retorna los 3 que están activos
        List<Doctor> soloActivos = List.of(
                buildDoctor(1, "d1", true),
                buildDoctor(2, "d2", true),
                buildDoctor(3, "d3", true)
                // d4 y d5 están inactivos → findAllActive no los incluye
        );
        when(doctorRepository.findAllActive()).thenReturn(soloActivos);

        // WHEN
        List<Doctor> result = doctorService.listActiveDoctors();

        // THEN
        assertEquals(3, result.size(), "Debe retornar exactamente 3 médicos activos");
        verify(doctorValidator).validateListNotEmpty(soloActivos);
        verify(doctorRepository).findAllActive(); // Solo se llama al método de activos
        verify(doctorRepository, never()).findById(anyInt()); // No se llama findById
    }

    @Test
    @DisplayName("listActiveDoctors: ningún médico activo -> validator lanza excepción de lista vacía")
    void listActiveDoctors_sinMedicosActivos_lanzaExcepcion() {
        List<Doctor> listaVacia = List.of();
        when(doctorRepository.findAllActive()).thenReturn(listaVacia);
        doThrow(new IllegalArgumentException("No hay médicos activos"))
                .when(doctorValidator).validateListNotEmpty(listaVacia);

        assertThrows(IllegalArgumentException.class, () -> doctorService.listActiveDoctors());
    }

    // -----------------------------------------------------------------------
    // modifyDoctor
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Modificar médico existente -> verifica existencia, valida datos y actualiza")
    void modifyDoctor_medicoExistente_verificaYActualiza() {
        Doctor doctor = buildDoctor(1, "clopez", true);
        when(doctorRepository.findById(1)).thenReturn(doctor);
        when(doctorRepository.update(doctor)).thenReturn(true);

        boolean result = doctorService.modifyDoctor(doctor);

        assertTrue(result);
        verify(doctorValidator).validateExists(doctor); // Verifica que existe
        verify(doctorValidator).validateDoctor(doctor); // Valida los nuevos datos
        verify(doctorRepository).update(doctor);
    }

    @Test
    @DisplayName("Modificar médico inexistente -> validator detecta null y lanza excepción")
    void modifyDoctor_medicoInexistente_lanzaExcepcion() {
        Doctor doctor = buildDoctor(99, "noexiste", true);
        when(doctorRepository.findById(99)).thenReturn(null);
        doThrow(new IllegalArgumentException("Médico no encontrado"))
                .when(doctorValidator).validateExists(null);

        assertThrows(IllegalArgumentException.class, () -> doctorService.modifyDoctor(doctor));
        verify(doctorRepository, never()).update(any());
    }

    // -----------------------------------------------------------------------
    // deactivateDoctor
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Desactivar médico existente -> verifica existencia y desactiva")
    void deactivateDoctor_medicoExistente_verificaYDesactiva() {
        Doctor doctor = buildDoctor(1, "clopez", true);
        when(doctorRepository.findById(1)).thenReturn(doctor);
        when(doctorRepository.deactivate(1)).thenReturn(true);

        boolean result = doctorService.deactivateDoctor(1);

        assertTrue(result);
        verify(doctorValidator).validateExists(doctor);
        verify(doctorRepository).deactivate(1);
    }

    @Test
    @DisplayName("Desactivar médico inexistente -> validator lanza excepción, no desactiva")
    void deactivateDoctor_medicoInexistente_lanzaExcepcion() {
        when(doctorRepository.findById(99)).thenReturn(null);
        doThrow(new IllegalArgumentException("Médico no encontrado"))
                .when(doctorValidator).validateExists(null);

        assertThrows(IllegalArgumentException.class, () -> doctorService.deactivateDoctor(99));
        verify(doctorRepository, never()).deactivate(anyInt());
    }
}