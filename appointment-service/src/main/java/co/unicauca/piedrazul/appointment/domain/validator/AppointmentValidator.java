package co.unicauca.piedrazul.appointment.domain.validator;

import co.unicauca.piedrazul.appointment.domain.entities.Appointment;

import java.util.List;

/**
 * Interfaz para validaciones de citas médicas
 * Permite agregar nuevas reglas sin modificar el servicio
 */
public interface AppointmentValidator {

    /**
     * Valida una cita antes de agendarla o reagendarla
     *
     * @param appointment    La cita a validar
     * @param existingOnDate Citas activas del mismo médico en la misma fecha
     */
    void validate(Appointment appointment, List<Appointment> existingOnDate);
}
