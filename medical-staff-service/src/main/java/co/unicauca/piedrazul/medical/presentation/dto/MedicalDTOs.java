package co.unicauca.piedrazul.medical.presentation.dto;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;

/**
 * DTOs del medical-staff-service.
 *
 * @author Ginner Ortega
 */
public class MedicalDTOs {

    public record DoctorResponse(
            int id,
            String fullName,
            String licenseNumber,
            List<String> specialties
    ) {}

    public record ScheduleResponse(
            int id,
            String dayName,   // nombre legible: Lunes, Martes...
            int dayOfWeek,    // número ISO: 1=Lunes...7=Domingo
            String startTime,
            String endTime,
            int intervalMinutes
    ) {}

    public record ScheduleUpdateRequest(
            @NotEmpty(message = "Debe seleccionar al menos un día")
            List<Integer> selectedDays,   // números ISO: [1,2,3,4,5]

            @Positive(message = "El intervalo debe ser mayor a 0")
            int interval,

            String startTime,
            String endTime
    ) {}

    public record SpecialtyResponse(
            int id,
            String name
    ) {}

    public record MessageResponse(String message) {}

    // --- Response completo del médico con horario y disponibilidad ---
    public record DoctorFullInfoResponse(
            DoctorResponse doctor,
            List<ScheduleResponse> schedules,
            List<String> availableSlots
    ) {}
}