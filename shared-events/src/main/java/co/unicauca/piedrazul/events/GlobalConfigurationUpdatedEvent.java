package co.unicauca.piedrazul.events;

/**
 * Evento publicado por configuration-service cuando se actualiza la configuración global.
 * Consumido por appointment-service y medical-staff-service para actualizar sus parámetros.
 *
 * @param parameterKey   Clave del parámetro actualizado (ej: "appointment_window_weeks")
 * @param parameterValue Nuevo valor del parámetro
 */
public record GlobalConfigurationUpdatedEvent(
        String parameterKey,
        String parameterValue
) {}
