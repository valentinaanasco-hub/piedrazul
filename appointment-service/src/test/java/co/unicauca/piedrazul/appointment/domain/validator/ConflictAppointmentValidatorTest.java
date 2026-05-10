package co.unicauca.piedrazul.appointment.domain.validator;

import co.unicauca.piedrazul.appointment.domain.entities.Appointment;
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
        Appointment existing = buildAppointment(1, LocalTime.of(10, 0));
        Appointment newAppointment = buildAppointment(0, LocalTime.of(9, 0));

        List<Appointment> existingList = List.of(existing);
        assertDoesNotThrow(() -> validator.validate(newAppointment, existingList));
    }

    @Test
    void validate_mismaHoraDiferenteCita_lanzaExcepcion() {
        Appointment existing = buildAppointment(1, LocalTime.of(9, 0));
        Appointment newAppointment = buildAppointment(0, LocalTime.of(9, 0));

        List<Appointment> existingList = List.of(existing);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> validator.validate(newAppointment, existingList)
        );
        assertEquals("Ya existe una cita para ese médico en esa fecha y hora", ex.getMessage());
    }

    @Test
    void validate_mismaHoraMismaCita_noLanzaExcepcion() {
        // Reagendamiento — misma cita, misma hora → no debe conflictuar consigo misma
        Appointment existing = buildAppointment(5, LocalTime.of(9, 0));
        Appointment sameAppointment = buildAppointment(5, LocalTime.of(9, 0));

        List<Appointment> existingList = List.of(existing);
        assertDoesNotThrow(() -> validator.validate(sameAppointment, existingList));
    }

    @Test
    void validate_variasHorasDisponibles_noLanzaExcepcion() {
        Appointment existing1 = buildAppointment(1, LocalTime.of(9, 0));
        Appointment existing2 = buildAppointment(2, LocalTime.of(10, 0));
        Appointment newAppointment = buildAppointment(0, LocalTime.of(11, 0));

        List<Appointment> existingList = List.of(existing1, existing2);
        assertDoesNotThrow(() -> validator.validate(newAppointment, existingList));
    }
}
