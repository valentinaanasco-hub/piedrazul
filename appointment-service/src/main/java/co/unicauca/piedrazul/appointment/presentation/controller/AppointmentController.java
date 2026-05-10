package co.unicauca.piedrazul.appointment.presentation.controller;

import co.unicauca.piedrazul.appointment.domain.entities.Appointment;
import co.unicauca.piedrazul.appointment.domain.service.IAppointmentService;
import co.unicauca.piedrazul.appointment.presentation.dto.AppointmentDTOs.AppointmentResponse;
import co.unicauca.piedrazul.appointment.presentation.dto.AppointmentDTOs.CreateAppointmentRequest;
import co.unicauca.piedrazul.appointment.presentation.dto.AppointmentDTOs.MessageResponse;
import co.unicauca.piedrazul.appointment.presentation.dto.AppointmentDTOs.RescheduleAppointmentRequest;
import co.unicauca.piedrazul.appointment.presentation.mapper.AppointmentMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * Controlador REST para gestión de citas médicas.
 *
 * @author Valentina Añasco
 * @author Camila Dorado
 * @author Felipe Gutierrez
 * @author Ginner Ortega
 * @author Santiago Solarte
 */
@RestController
@RequestMapping("/api/v1/appointments")
@Tag(name = "Appointments", description = "Gestión de citas médicas")
public class AppointmentController {

    private final IAppointmentService appointmentService;
    private final AppointmentMapper appointmentMapper;

    public AppointmentController(IAppointmentService appointmentService,
                                  AppointmentMapper appointmentMapper) {
        this.appointmentService = appointmentService;
        this.appointmentMapper = appointmentMapper;
    }

    @GetMapping("/doctor/{doctorId}/date/{date}")
    @Operation(summary = "Listar citas por médico y fecha",
               description = "Retorna las citas de un médico en una fecha específica")
    public ResponseEntity<List<AppointmentResponse>> listByDoctorAndDate(
            @PathVariable int doctorId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        List<AppointmentResponse> appointments = appointmentService
                .listByDoctorAndDate(doctorId, date)
                .stream()
                .map(appointmentMapper::toResponse)
                .toList();

        return ResponseEntity.ok(appointments);
    }

    @PostMapping
    @Operation(summary = "Crear cita manual",
               description = "El agendador registra una nueva cita para un paciente")
    public ResponseEntity<?> createAppointment(@Valid @RequestBody CreateAppointmentRequest request) {
        try {
            Appointment appointment = appointmentMapper.toEntity(request);
            Appointment saved = appointmentService.scheduleAppointment(appointment);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(appointmentMapper.toResponse(saved));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new MessageResponse(e.getMessage()));
        }
    }

    @PatchMapping("/{id}/reschedule")
    @Operation(summary = "Reagendar cita")
    public ResponseEntity<?> reschedule(@PathVariable int id,
                                         @Valid @RequestBody RescheduleAppointmentRequest request) {
        try {
            Appointment updated = appointmentService.rescheduleAppointment(
                    id, request.newDate(), request.newStartTime(), request.newEndTime());
            return ResponseEntity.ok(appointmentMapper.toResponse(updated));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new MessageResponse(e.getMessage()));
        }
    }

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Cancelar cita")
    public ResponseEntity<?> cancel(@PathVariable int id) {
        try {
            Appointment cancelled = appointmentService.cancelAppointment(id);
            return ResponseEntity.ok(appointmentMapper.toResponse(cancelled));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse(e.getMessage()));
        }
    }

    @PatchMapping("/{id}/attend")
    @Operation(summary = "Marcar cita como atendida")
    public ResponseEntity<?> markAsAttended(@PathVariable int id) {
        try {
            Appointment attended = appointmentService.markAsAttended(id);
            return ResponseEntity.ok(appointmentMapper.toResponse(attended));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse(e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar cita por ID")
    public ResponseEntity<?> findById(@PathVariable int id) {
        try {
            return ResponseEntity.ok(appointmentMapper.toResponse(appointmentService.findById(id)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse(e.getMessage()));
        }
    }

    @GetMapping
    @Operation(summary = "Listar todas las citas")
    public ResponseEntity<List<AppointmentResponse>> listAll() {
        List<AppointmentResponse> appointments = appointmentService.listAll()
                .stream()
                .map(appointmentMapper::toResponse)
                .toList();
        return ResponseEntity.ok(appointments);
    }

    @GetMapping("/patient/{patientId}")
    @Operation(summary = "Listar citas de un paciente")
    public ResponseEntity<List<AppointmentResponse>> listByPatient(@PathVariable int patientId) {
        List<AppointmentResponse> appointments = appointmentService.listByPatient(patientId)
                .stream()
                .map(appointmentMapper::toResponse)
                .toList();
        return ResponseEntity.ok(appointments);
    }
}
