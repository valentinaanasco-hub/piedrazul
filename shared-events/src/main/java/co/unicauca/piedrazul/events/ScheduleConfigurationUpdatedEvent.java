package co.unicauca.piedrazul.events;

import java.time.LocalTime;
import java.util.List;

/**
 * Evento publicado por configuration-service cuando se actualiza la configuración
 * de horarios de un profesional.
 * Consumido por medical-staff-service para actualizar sus horarios.
 *
 * @param doctorId         ID del médico/profesional
 * @param schedules        Lista de horarios configurados
 */
public record ScheduleConfigurationUpdatedEvent(
        int doctorId,
        List<ScheduleItem> schedules
) {
    /**
     * Item individual de horario para un día de la semana.
     */
    public record ScheduleItem(
            int dayOfWeek,
            LocalTime startTime,
            LocalTime endTime,
            int intervalMinutes
    ) {}
}
