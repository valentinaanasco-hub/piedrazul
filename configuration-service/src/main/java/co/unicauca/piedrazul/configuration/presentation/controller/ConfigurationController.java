package co.unicauca.piedrazul.configuration.presentation.controller;

import co.unicauca.piedrazul.configuration.domain.entities.DoctorScheduleConfiguration;
import co.unicauca.piedrazul.configuration.domain.service.ConfigurationService;
import co.unicauca.piedrazul.configuration.presentation.dto.ConfigurationDTOs.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Controlador REST para gestión de configuración del sistema.
 * Maneja tanto configuración global como configuración por profesional.
 * Publica eventos asíncronos cuando se actualizan configuraciones.
 *
 * @author Santiago Solarte
 */
@RestController
@RequestMapping("/api/v1/configuration")
@Tag(name = "Configuration", description = "Gestión de configuración del sistema")
public class ConfigurationController {

    private final ConfigurationService configurationService;

    public ConfigurationController(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    // ========== CONFIGURACIÓN GLOBAL ==========

    @GetMapping("/global")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENDADOR', 'DOCTOR')")
    @Operation(summary = "Obtener configuración global",
               description = "Retorna la configuración global del sistema (ventana de tiempo para agendar citas)")
    public ResponseEntity<GlobalConfigurationResponse> getGlobalConfiguration() {
        int weeks = configurationService.getAppointmentWindowWeeks();
        return ResponseEntity.ok(new GlobalConfigurationResponse(weeks));
    }

    @PutMapping("/global/appointment-window")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actualizar ventana de tiempo para agendar citas",
               description = "Define cuántas semanas hacia adelante se pueden agendar citas. Publica evento asíncrono.")
    public ResponseEntity<?> updateAppointmentWindow(
            @Valid @RequestBody UpdateAppointmentWindowRequest request) {
        try {
            configurationService.updateAppointmentWindowWeeks(request.weeks());
            return ResponseEntity.ok(
                    new ConfigurationMessageResponse("Configuración actualizada exitosamente"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ConfigurationMessageResponse(e.getMessage()));
        }
    }

    // ========== CONFIGURACIÓN POR PROFESIONAL ==========

    @GetMapping("/doctor/{doctorId}/schedule")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Obtener horarios de un profesional",
               description = "Retorna la configuración de días, horarios e intervalos de un profesional")
    public ResponseEntity<List<DoctorScheduleResponse>> getDoctorSchedule(@PathVariable int doctorId) {
        List<DoctorScheduleConfiguration> schedules = configurationService.getDoctorSchedule(doctorId);
        List<DoctorScheduleResponse> response = schedules.stream()
                .map(this::toScheduleResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/doctor/{doctorId}/schedule")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actualizar horarios de un profesional",
               description = "Define los días de atención, franja horaria e intervalo entre citas. Publica evento asíncrono.")
    public ResponseEntity<?> updateDoctorSchedule(
            @PathVariable int doctorId,
            @Valid @RequestBody UpdateDoctorScheduleRequest request) {
        try {
            // Validar horarios
            for (ScheduleItemRequest item : request.schedules()) {
                if (!item.startTime().isBefore(item.endTime())) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(new ConfigurationMessageResponse(
                                    "La hora de inicio debe ser anterior a la hora de fin"));
                }
            }

            // Convertir DTOs a entidades
            List<DoctorScheduleConfiguration> schedules = new ArrayList<>();
            for (ScheduleItemRequest item : request.schedules()) {
                DoctorScheduleConfiguration schedule = new DoctorScheduleConfiguration();
                schedule.setDoctorId(doctorId);
                schedule.setDayOfWeek(item.dayOfWeek());
                schedule.setStartTime(item.startTime());
                schedule.setEndTime(item.endTime());
                schedule.setIntervalMinutes(item.intervalMinutes());
                schedules.add(schedule);
            }

            // Actualizar horarios (publica evento automáticamente)
            List<DoctorScheduleConfiguration> saved = configurationService.updateDoctorSchedule(doctorId, schedules);
            List<DoctorScheduleResponse> response = saved.stream()
                    .map(this::toScheduleResponse)
                    .toList();

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ConfigurationMessageResponse(e.getMessage()));
        }
    }

    @DeleteMapping("/doctor/{doctorId}/schedule")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar horarios de un profesional",
               description = "Elimina toda la configuración de horarios de un profesional. Publica evento asíncrono.")
    public ResponseEntity<?> deleteDoctorSchedule(@PathVariable int doctorId) {
        try {
            configurationService.deleteDoctorSchedule(doctorId);
            return ResponseEntity.ok(
                    new ConfigurationMessageResponse("Horarios eliminados exitosamente"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ConfigurationMessageResponse(e.getMessage()));
        }
    }

    // ========== MÉTODOS AUXILIARES ==========

    private DoctorScheduleResponse toScheduleResponse(DoctorScheduleConfiguration schedule) {
        String dayName = getDayName(schedule.getDayOfWeek());
        return new DoctorScheduleResponse(
                schedule.getId(),
                schedule.getDoctorId(),
                schedule.getDayOfWeek(),
                dayName,
                schedule.getStartTime(),
                schedule.getEndTime(),
                schedule.getIntervalMinutes()
        );
    }

    private String getDayName(int dayOfWeek) {
        return switch (dayOfWeek) {
            case 1 -> "Lunes";
            case 2 -> "Martes";
            case 3 -> "Miércoles";
            case 4 -> "Jueves";
            case 5 -> "Viernes";
            case 6 -> "Sábado";
            case 7 -> "Domingo";
            default -> "Desconocido";
        };
    }
}
