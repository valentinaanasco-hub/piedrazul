package co.unicauca.piedrazul.appointment.presentation.dto;

import co.unicauca.piedrazul.appointment.domain.entities.enums.AppointmentStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * DTOs del Appointment Service.
 *
 * @author Valentina Añasco
 * @author Camila Dorado
 * @author Felipe Gutierrez
 * @author Ginner Ortega
 * @author Santiago Solarte
 */
public class AppointmentDTOs {

    // --- Request para crear una cita ---
    public record CreateAppointmentRequest(
            @Positive(message = "El ID del médico debe ser positivo")
            int doctorId,

            @Positive(message = "El ID del paciente debe ser positivo")
            int patientId,

            @NotNull(message = "La fecha es obligatoria")
            LocalDate date,

            @NotNull(message = "La hora de inicio es obligatoria")
            LocalTime startTime,

            @NotNull(message = "La hora de fin es obligatoria")
            LocalTime endTime,

            String reason,
            String notes
    ) {}

    // --- Request para reagendar una cita ---
    public record RescheduleAppointmentRequest(
            @NotNull(message = "La nueva fecha es obligatoria")
            LocalDate newDate,

            @NotNull(message = "La nueva hora de inicio es obligatoria")
            LocalTime newStartTime,

            @NotNull(message = "La nueva hora de fin es obligatoria")
            LocalTime newEndTime
    ) {}

    // --- Response de una cita ---
    public record AppointmentResponse(
            int appointmentId,
            int doctorId,
            int patientId,
            LocalDate date,
            LocalTime startTime,
            LocalTime endTime,
            AppointmentStatus status,
            String reason,
            String notes
    ) {}

    // --- Response genérico ---
    public record MessageResponse(String message) {}
}
