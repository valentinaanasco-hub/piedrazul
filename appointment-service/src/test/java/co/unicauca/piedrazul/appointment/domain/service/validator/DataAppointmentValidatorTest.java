package co.unicauca.piedrazul.appointment.domain.service.validator;

import co.unicauca.piedrazul.appointment.domain.exception.AppointmentValidationException;
import co.unicauca.piedrazul.appointment.domain.model.Appointment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Pruebas unitarias para DataAppointmentValidator.
 * Verifica que los datos básicos de una cita sean correctos.
 */
class DataAppointmentValidatorTest {

    private DataAppointmentValidator validator;
    private List<Appointment>        emptyList;

    @BeforeEach
    void setUp() {
        validator = new DataAppointmentValidator();
        emptyList = new ArrayList<>();
    }

    private Appointment buildValidAppointment() {
        Appointment appointment = new Appointment();
        appointment.setDoctorId(1);
        appointment.setPatientId(2);
        appointment.setDate(LocalDate.of(2026, 6, 10));
        appointment.setStartTime(LocalTime.of(9, 0));
        appointment.setEndTime(LocalTime.of(9, 30));
        return appointment;
    }

    @Test
    void validate_datosCorrectos_noLanzaExcepcion() {
        assertDoesNotThrow(() -> validator.validate(buildValidAppointment(), emptyList));
    }

    @Test
    void validate_fechaNula_lanzaExcepcion() {
        Appointment appointment = buildValidAppointment();
        appointment.setDate(null);
        AppointmentValidationException ex = assertThrows(
                AppointmentValidationException.class,
                () -> validator.validate(appointment, emptyList)
        );
        assertEquals("La fecha de la cita es obligatoria", ex.getMessage());
    }

    @Test
    void validate_horaInicioNula_lanzaExcepcion() {
        Appointment appointment = buildValidAppointment();
        appointment.setStartTime(null);
        AppointmentValidationException ex = assertThrows(
                AppointmentValidationException.class,
                () -> validator.validate(appointment, emptyList)
        );
        assertEquals("La hora de inicio y fin son obligatorias", ex.getMessage());
    }

    @Test
    void validate_horaFinNula_lanzaExcepcion() {
        Appointment appointment = buildValidAppointment();
        appointment.setEndTime(null);
        AppointmentValidationException ex = assertThrows(
                AppointmentValidationException.class,
                () -> validator.validate(appointment, emptyList)
        );
        assertEquals("La hora de inicio y fin son obligatorias", ex.getMessage());
    }

    @Test
    void validate_horaInicioIgualAFin_lanzaExcepcion() {
        Appointment appointment = buildValidAppointment();
        appointment.setStartTime(LocalTime.of(10, 0));
        appointment.setEndTime(LocalTime.of(10, 0));
        AppointmentValidationException ex = assertThrows(
                AppointmentValidationException.class,
                () -> validator.validate(appointment, emptyList)
        );
        assertEquals("La hora de inicio debe ser anterior a la hora de fin", ex.getMessage());
    }

    @Test
    void validate_horaInicioDespuesDeHoraFin_lanzaExcepcion() {
        Appointment appointment = buildValidAppointment();
        appointment.setStartTime(LocalTime.of(11, 0));
        appointment.setEndTime(LocalTime.of(10, 0));
        assertThrows(AppointmentValidationException.class,
                () -> validator.validate(appointment, emptyList));
    }

    @Test
    void validate_doctorIdCero_lanzaExcepcion() {
        Appointment appointment = buildValidAppointment();
        appointment.setDoctorId(0);
        AppointmentValidationException ex = assertThrows(
                AppointmentValidationException.class,
                () -> validator.validate(appointment, emptyList)
        );
        assertEquals("El ID del médico debe ser positivo", ex.getMessage());
    }

    @Test
    void validate_pacienteIdNegativo_lanzaExcepcion() {
        Appointment appointment = buildValidAppointment();
        appointment.setPatientId(-1);
        AppointmentValidationException ex = assertThrows(
                AppointmentValidationException.class,
                () -> validator.validate(appointment, emptyList)
        );
        assertEquals("El ID del paciente debe ser positivo", ex.getMessage());
    }
}
