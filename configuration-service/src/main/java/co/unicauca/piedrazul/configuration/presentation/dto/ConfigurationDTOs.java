package co.unicauca.piedrazul.configuration.presentation.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalTime;
import java.util.List;

/**
 * DTOs para el módulo de configuración del sistema.
 *
 * @author Santiago Solarte
 */
public class ConfigurationDTOs {

    // ========== CONFIGURACIÓN GLOBAL ==========

    public record UpdateAppointmentWindowRequest(
            @NotNull(message = "La ventana de tiempo es obligatoria")
            @Min(value = 1, message = "La ventana de tiempo debe ser al menos 1 semana")
            @Max(value = 52, message = "La ventana de tiempo no puede exceder 52 semanas")
            Integer weeks
    ) {}

    public record GlobalConfigurationResponse(
            int appointmentWindowWeeks
    ) {}

    // ========== CONFIGURACIÓN POR PROFESIONAL ==========

    public record UpdateDoctorScheduleRequest(
            @NotNull(message = "Los horarios son obligatorios")
            List<ScheduleItemRequest> schedules
    ) {}

    public record ScheduleItemRequest(
            @NotNull(message = "El día de la semana es obligatorio")
            @Min(value = 1, message = "El día debe estar entre 1 (Lunes) y 7 (Domingo)")
            @Max(value = 7, message = "El día debe estar entre 1 (Lunes) y 7 (Domingo)")
            Integer dayOfWeek,

            @NotNull(message = "La hora de inicio es obligatoria")
            LocalTime startTime,

            @NotNull(message = "La hora de fin es obligatoria")
            LocalTime endTime,

            @NotNull(message = "El intervalo entre citas es obligatorio")
            @Positive(message = "El intervalo debe ser positivo")
            Integer intervalMinutes
    ) {}

    public record DoctorScheduleResponse(
            int configId,
            int doctorId,
            int dayOfWeek,
            String dayName,
            LocalTime startTime,
            LocalTime endTime,
            int intervalMinutes
    ) {}

    public record ConfigurationMessageResponse(
            String message
    ) {}
}
