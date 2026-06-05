package co.unicauca.piedrazul.appointment.infrastructure.in.rest.dto;

import co.unicauca.piedrazul.appointment.domain.model.AppointmentStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * DTOs del Appointment Service.
 * Viven en la capa de infraestructura — el dominio no los conoce.
 */
public class AppointmentDTOs {

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

    public record RescheduleAppointmentRequest(
            Integer newDoctorId,   // opcional: nuevo profesional/servicio (si se cambia)

            @NotNull(message = "La nueva fecha es obligatoria")
            LocalDate newDate,

            @NotNull(message = "La nueva hora de inicio es obligatoria")
            LocalTime newStartTime,

            @NotNull(message = "La nueva hora de fin es obligatoria")
            LocalTime newEndTime
    ) {}

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

    public record MessageResponse(String message) {}
}
