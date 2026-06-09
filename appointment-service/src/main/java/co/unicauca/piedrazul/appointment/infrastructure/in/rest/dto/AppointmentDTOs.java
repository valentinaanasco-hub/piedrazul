package co.unicauca.piedrazul.appointment.infrastructure.in.rest.dto;

import co.unicauca.piedrazul.appointment.domain.model.AppointmentStatus;
import co.unicauca.piedrazul.appointment.domain.model.ServiceType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * DTOs del Appointment Service.
 * Viven en la capa de infraestructura — el dominio no los conoce.
 */
public class AppointmentDTOs {

    public record CreateAppointmentRequest(
            @Positive(message = "El ID del médico debe ser positivo")
            long doctorId,

            @NotNull(message = "El nombre del doctor no puede estar vacio")
            String doctorName,

            @Positive(message = "El ID del paciente debe ser positivo")
            long patientId,

            @NotNull(message = "La fecha es obligatoria")
            LocalDate date,

            @NotNull(message = "La hora de inicio es obligatoria")
            LocalTime startTime,

            @NotNull(message = "La hora de fin es obligatoria")
            LocalTime endTime,

            String reason,
            String notes,

            @NotNull(message = "El tipo de servicio es obligatorio")
            ServiceType serviceType
    ) {}

    public record RescheduleAppointmentRequest(
            Long newDoctorId,   // opcional: nuevo profesional/servicio (si se cambia)

            @NotNull(message = "El nombre del doctor no puede estar vacio")
            String doctorName,

            @NotNull(message = "El tipo de servicio es obligatorio")
            ServiceType serviceType,

            @NotNull(message = "La nueva fecha es obligatoria")
            LocalDate newDate,

            @NotNull(message = "La nueva hora de inicio es obligatoria")
            LocalTime newStartTime,

            @NotNull(message = "La nueva hora de fin es obligatoria")
            LocalTime newEndTime
    ) {}

    public record AppointmentResponse(
            int appointmentId,
            long doctorId,
            String doctorName,
            long patientId,
            LocalDate date,
            LocalTime startTime,
            LocalTime endTime,
            AppointmentStatus status,
            String reason,
            String notes,
            ServiceType serviceType
    ) {}

    public record MessageResponse(String message) {}

    /**
     * Cuerpo opcional del endpoint PATCH /{id}/attend.
     * Si authorizedServiceType es null o no se envía el body, no se crea autorización.
     */
    public record AttendAppointmentRequest(ServiceType authorizedServiceType) {}

    /**
     * Respuesta del endpoint GET /patient/{id}/authorization.
     * Representa la autorización de servicio activa del paciente.
     */
    public record PatientAuthorizationResponse(
            int authId,
            ServiceType serviceType,
            LocalDateTime authorizedAt,
            LocalDateTime expiresAt
    ) {}
}
