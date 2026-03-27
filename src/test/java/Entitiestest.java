package co.unicauca.piedrazul.domain.entities;

import co.unicauca.piedrazul.domain.entities.enums.AppointmentStatus;
import co.unicauca.piedrazul.domain.entities.enums.UserState;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias sobre las entidades del dominio.
 *
 * No necesitan mocks porque prueban solo la lógica interna de los objetos
 * (getters, setters, constructores, getFullName).
 */
class EntitiesTest {

    // -----------------------------------------------------------------------
    // User.getFullName()
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("getFullName: todos los campos completos -> retorna nombre con espacios")
    void getFullName_todosLosCampos_retornaNombreCompleto() {
        User user = new User();
        user.setFirstName("Juan");
        user.setMiddleName("Carlos");
        user.setFirstSurname("Perez");
        user.setLastName("Gomez");

        assertEquals("Juan Carlos Perez Gomez", user.getFullName());
    }

    @Test
    @DisplayName("getFullName: sin segundo nombre -> no deja espacio doble")
    void getFullName_sinSegundoNombre_sinEspacioDoble() {
        User user = new User();
        user.setFirstName("Maria");
        user.setMiddleName(null);
        user.setFirstSurname("Lopez");
        user.setLastName("Torres");

        // No debe quedar "Maria  Lopez Torres" con doble espacio
        assertEquals("Maria Lopez Torres", user.getFullName());
    }

    @Test
    @DisplayName("getFullName: campos con espacios en blanco -> recorta correctamente")
    void getFullName_camposConEspacios_recortaCorrectamente() {
        User user = new User();
        user.setFirstName("  Ana  ");
        user.setMiddleName("  "); // Solo espacios → debe ignorarse
        user.setFirstSurname("  Rios  ");
        user.setLastName("  "); // Solo espacios → debe ignorarse

        assertEquals("Ana Rios", user.getFullName());
    }

    @Test
    @DisplayName("getFullName: todos los campos null -> retorna cadena vacía")
    void getFullName_todosNull_retornaCadenaVacia() {
        User user = new User();
        // Ningún campo seteado → debe retornar "" y no lanzar NullPointerException
        assertDoesNotThrow(() -> {
            String nombre = user.getFullName();
            assertEquals("", nombre);
        });
    }

    // -----------------------------------------------------------------------
    // User — estado ACTIVO / INACTIVO
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("User: estado ACTIVO -> getter retorna ACTIVO")
    void user_estadoActivo_getterRetornaActivo() {
        User u = new User();
        u.setState(UserState.ACTIVO);
        assertEquals(UserState.ACTIVO, u.getState());
    }

    @Test
    @DisplayName("User: estado INACTIVO -> getter retorna INACTIVO")
    void user_estadoInactivo_getterRetornaInactivo() {
        User u = new User();
        u.setState(UserState.INACTIVO);
        assertEquals(UserState.INACTIVO, u.getState());
    }

    // -----------------------------------------------------------------------
    // Appointment — estado y constructores
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Appointment: recién creada tiene estado null por defecto")
    void appointment_nueva_estadoNullPorDefecto() {
        Appointment a = new Appointment();
        assertNull(a.getStatus(), "Una Appointment nueva no debe tener estado predefinido");
    }

    @Test
    @DisplayName("Appointment: cambiar estado a CANCELADA -> getter refleja el cambio")
    void appointment_setStatus_cancelada_getterRefleja() {
        Appointment a = new Appointment();
        a.setStatus(AppointmentStatus.CANCELADA);
        assertEquals(AppointmentStatus.CANCELADA, a.getStatus());
    }

    @Test
    @DisplayName("Appointment: constructor completo inicializa todos los campos")
    void appointment_constructorCompleto_inicializaTodosCampos() {
        Doctor doctor = new Doctor();
        Patient patient = new Patient();
        LocalDate date = LocalDate.of(2025, 6, 2);
        LocalTime start = LocalTime.of(8, 0);
        LocalTime end = LocalTime.of(8, 30);

        Appointment a = new Appointment(1, date, start, end, AppointmentStatus.AGENDADA, doctor, patient);

        assertEquals(1, a.getAppointmentId());
        assertEquals(date, a.getDate());
        assertEquals(start, a.getStartTime());
        assertEquals(end, a.getEndTime());
        assertEquals(AppointmentStatus.AGENDADA, a.getStatus());
        assertNotNull(a.getDoctor());
        assertNotNull(a.getPatient());
    }

    @Test
    @DisplayName("Appointment: transición de estados AGENDADA -> REAGENDADA -> ATENDIDA")
    void appointment_transicionDeEstados_correcta() {
        Appointment a = new Appointment();

        a.setStatus(AppointmentStatus.AGENDADA);
        assertEquals(AppointmentStatus.AGENDADA, a.getStatus());

        a.setStatus(AppointmentStatus.REAGENDADA);
        assertEquals(AppointmentStatus.REAGENDADA, a.getStatus());

        a.setStatus(AppointmentStatus.ATENDIDA);
        assertEquals(AppointmentStatus.ATENDIDA, a.getStatus());
    }

    // -----------------------------------------------------------------------
    // Patient — herencia de User
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Patient: hereda getFullName correctamente de User")
    void patient_heredaGetFullName() {
        Patient p = new Patient();
        p.setFirstName("Laura");
        p.setFirstSurname("Cano");

        assertEquals("Laura Cano", p.getFullName());
    }

    @Test
    @DisplayName("Patient: getters y setters de campos propios funcionan")
    void patient_gettersYSettersPropios_funcionan() {
        Patient p = new Patient();
        p.setPhone("3001234567");
        p.setEmail("laura@mail.com");
        p.setGender("F");
        p.setBirthDay("15");
        p.setBirthMonth("03");
        p.setBirthYear("1990");

        assertEquals("3001234567", p.getPhone());
        assertEquals("laura@mail.com", p.getEmail());
        assertEquals("F", p.getGender());
        assertEquals("15", p.getBirthDay());
        assertEquals("03", p.getBirthMonth());
        assertEquals("1990", p.getBirthYear());
    }

    // -----------------------------------------------------------------------
    // DoctorSchedule
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("DoctorSchedule: getters y setters funcionan correctamente")
    void doctorSchedule_gettersYSetters_funcionan() {
        DoctorSchedule ds = new DoctorSchedule();
        ds.setScheduleId(1);
        ds.setDayOfWeek(2); // Martes
        ds.setStartTime(LocalTime.of(8, 0));
        ds.setEndTime(LocalTime.of(12, 0));
        ds.setIntervalMinutes(30);

        assertEquals(1, ds.getScheduleId());
        assertEquals(2, ds.getDayOfWeek());
        assertEquals(LocalTime.of(8, 0), ds.getStartTime());
        assertEquals(LocalTime.of(12, 0), ds.getEndTime());
        assertEquals(30, ds.getIntervalMinutes());
    }

    // -----------------------------------------------------------------------
    // SystemParameter
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("SystemParameter: constructor y getters funcionan correctamente")
    void systemParameter_constructorYGetters_correctos() {
        SystemParameter sp = new SystemParameter("SEMANAS_AGENDAMIENTO", "4");

        assertEquals("SEMANAS_AGENDAMIENTO", sp.getKey());
        assertEquals("4", sp.getValue());
    }

    // -----------------------------------------------------------------------
    // Specialty
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Specialty: constructor y getters funcionan correctamente")
    void specialty_constructorYGetters_correctos() {
        Specialty s = new Specialty(1, "Fisioterapia");

        assertEquals(1, s.getSpecialtyId());
        assertEquals("Fisioterapia", s.getSpecialtyName());
    }

    // -----------------------------------------------------------------------
    // Role
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Role: constructor y getters funcionan correctamente")
    void role_constructorYGetters_correctos() {
        Role r = new Role(2, "MEDICO");

        assertEquals(2, r.getRoleId());
        assertEquals("MEDICO", r.getRoleName());
    }
}