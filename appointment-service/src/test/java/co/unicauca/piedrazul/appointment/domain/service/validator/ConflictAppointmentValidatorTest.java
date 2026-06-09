package co.unicauca.piedrazul.appointment.domain.service.validator;

import co.unicauca.piedrazul.appointment.domain.exception.AppointmentConflictException;
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
 * Pruebas unitarias para ConflictAppointmentValidator.
 * Verifica que no existan conflictos de horario para el médico.
 */
class ConflictAppointmentValidatorTest {

    private ConflictAppointmentValidator validator;

    @BeforeEach
    void setUp() {
        validator = new ConflictAppointmentValidator();
    }

    private Appointment buildAppointment(int id, LocalTime startTime) {
        Appointment appointment = new Appointment();
        appointment.setAppointmentId(id);
        appointment.setDoctorId(1);
        appointment.setPatientId(2);
        appointment.setDate(LocalDate.of(2026, 6, 10));
        appointment.setStartTime(startTime);
        appointment.setEndTime(startTime.plusMinutes(30));
        return appointment;
    }

    @Test
    void validate_sinCitasExistentes_noLanzaExcepcion() {
        Appointment appointment = buildAppointment(0, LocalTime.of(9, 0));
        assertDoesNotThrow(() -> validator.validate(appointment, new ArrayList<>()));
    }

    @Test
    void validate_horaDiferente_noLanzaExcepcion() {
        Appointment existing       = buildAppointment(1, LocalTime.of(10, 0));
        Appointment newAppointment = buildAppointment(0, LocalTime.of(9, 0));
        assertDoesNotThrow(() -> validator.validate(newAppointment, List.of(existing)));
    }

    @Test
    void validate_mismaHoraDiferenteCita_lanzaExcepcion() {
        Appointment existing       = buildAppointment(1, LocalTime.of(9, 0));
        Appointment newAppointment = buildAppointment(0, LocalTime.of(9, 0));

        AppointmentConflictException ex = assertThrows(
                AppointmentConflictException.class,
                () -> validator.validate(newAppointment, List.of(existing))
        );
        assertEquals("Ya existe una cita para ese médico en esa fecha y hora", ex.getMessage());
    }

    @Test
    void validate_mismaHoraMismaCita_noLanzaExcepcion() {
        Appointment existing        = buildAppointment(5, LocalTime.of(9, 0));
        Appointment sameAppointment = buildAppointment(5, LocalTime.of(9, 0));
        assertDoesNotThrow(() -> validator.validate(sameAppointment, List.of(existing)));
    }

    @Test
    void validate_variasHorasDisponibles_noLanzaExcepcion() {
        Appointment existing1      = buildAppointment(1, LocalTime.of(9, 0));
        Appointment existing2      = buildAppointment(2, LocalTime.of(10, 0));
        Appointment newAppointment = buildAppointment(0, LocalTime.of(11, 0));
        assertDoesNotThrow(() -> validator.validate(newAppointment, List.of(existing1, existing2)));
    }
}
