package co.unicauca.piedrazul.appointment.domain.entities.enums;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Estados posibles de una cita médica
 */
public enum AppointmentStatus {
    AGENDADA,
    REAGENDADA,
    CANCELADA,
    ATENDIDA,
    @JsonProperty("NO ASISTIO")
    NO_ASISTIO
}
