package co.unicauca.piedrazul.appointment.infrastructure.in.rest;

import co.unicauca.piedrazul.appointment.domain.model.Appointment;
import co.unicauca.piedrazul.appointment.domain.port.in.AppointmentUseCase;
import co.unicauca.piedrazul.appointment.infrastructure.in.rest.dto.AppointmentDTOs.AppointmentResponse;
import co.unicauca.piedrazul.appointment.infrastructure.in.rest.dto.AppointmentDTOs.CreateAppointmentRequest;
import co.unicauca.piedrazul.appointment.infrastructure.in.rest.dto.AppointmentDTOs.MessageResponse;
import co.unicauca.piedrazul.appointment.infrastructure.in.rest.dto.AppointmentDTOs.RescheduleAppointmentRequest;
import co.unicauca.piedrazul.appointment.infrastructure.in.rest.mapper.AppointmentDtoMapper;
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
 * Adaptador de entrada REST para gestión de citas médicas.
 * Depende del puerto de entrada AppointmentUseCase — no del servicio directamente.
 */
@RestController
@RequestMapping("/api/v1/appointments")
@Tag(name = "Appointments", description = "Gestión de citas médicas")
public class AppointmentController {

    private final AppointmentUseCase  appointmentUseCase;
    private final AppointmentDtoMapper   appointmentMapper;

    public AppointmentController(AppointmentUseCase appointmentUseCase,
                                  AppointmentDtoMapper appointmentMapper) {
        this.appointmentUseCase = appointmentUseCase;
        this.appointmentMapper  = appointmentMapper;
    }

    @GetMapping("/doctor/{doctorId}/date/{date}")
    @Operation(summary = "Listar citas por médico y fecha")
    public ResponseEntity<List<AppointmentResponse>> listByDoctorAndDate(
            @PathVariable int doctorId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        return ResponseEntity.ok(
                appointmentUseCase.listByDoctorAndDate(doctorId, date)
                        .stream().map(appointmentMapper::toResponse).toList());
    }

    @PostMapping
    @Operation(summary = "Crear cita manual")
    public ResponseEntity<?> createAppointment(@Valid @RequestBody CreateAppointmentRequest request) {
        try {
            Appointment appointment = appointmentMapper.toEntity(request);
            Appointment saved       = appointmentUseCase.scheduleAppointment(appointment);
            return ResponseEntity.status(HttpStatus.CREATED).body(appointmentMapper.toResponse(saved));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new MessageResponse(e.getMessage()));
        }
    }

    @PatchMapping("/{id}/reschedule")
    @Operation(summary = "Reagendar cita")
    public ResponseEntity<?> reschedule(@PathVariable int id,
                                         @Valid @RequestBody RescheduleAppointmentRequest request) {
        try {
            Appointment updated = appointmentUseCase.rescheduleAppointment(
                    id, request.newDoctorId(), request.newDate(), request.newStartTime(), request.newEndTime());
            return ResponseEntity.ok(appointmentMapper.toResponse(updated));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new MessageResponse(e.getMessage()));
        }
    }

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Cancelar cita")
    public ResponseEntity<?> cancel(@PathVariable int id) {
        try {
            return ResponseEntity.ok(appointmentMapper.toResponse(
                    appointmentUseCase.cancelAppointment(id)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse(e.getMessage()));
        }
    }

    @PatchMapping("/{id}/attend")
    @Operation(summary = "Marcar cita como atendida")
    public ResponseEntity<?> markAsAttended(@PathVariable int id) {
        try {
            return ResponseEntity.ok(appointmentMapper.toResponse(
                    appointmentUseCase.markAsAttended(id)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse(e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar cita por ID")
    public ResponseEntity<?> findById(@PathVariable int id) {
        try {
            return ResponseEntity.ok(appointmentMapper.toResponse(
                    appointmentUseCase.findById(id)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse(e.getMessage()));
        }
    }

    @GetMapping
    @Operation(summary = "Listar todas las citas")
    public ResponseEntity<List<AppointmentResponse>> listAll() {
        return ResponseEntity.ok(
                appointmentUseCase.listAll().stream().map(appointmentMapper::toResponse).toList());
    }

    @GetMapping("/patient/{patientId}")
    @Operation(summary = "Listar citas de un paciente")
    public ResponseEntity<List<AppointmentResponse>> listByPatient(@PathVariable int patientId) {
        return ResponseEntity.ok(
                appointmentUseCase.listByPatient(patientId)
                        .stream().map(appointmentMapper::toResponse).toList());
    }
}
