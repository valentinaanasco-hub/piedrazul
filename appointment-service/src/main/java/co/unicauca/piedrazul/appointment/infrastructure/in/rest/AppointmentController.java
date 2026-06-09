package co.unicauca.piedrazul.appointment.infrastructure.in.rest;

import co.unicauca.piedrazul.appointment.domain.model.Appointment;
import co.unicauca.piedrazul.appointment.domain.port.in.AppointmentUseCase;
import co.unicauca.piedrazul.appointment.domain.model.ServiceType;
import co.unicauca.piedrazul.appointment.infrastructure.in.rest.dto.AppointmentDTOs.AppointmentResponse;
import co.unicauca.piedrazul.appointment.infrastructure.in.rest.dto.AppointmentDTOs.AttendAppointmentRequest;
import co.unicauca.piedrazul.appointment.infrastructure.in.rest.dto.AppointmentDTOs.CreateAppointmentRequest;
import co.unicauca.piedrazul.appointment.infrastructure.in.rest.dto.AppointmentDTOs.PatientAuthorizationResponse;
import co.unicauca.piedrazul.appointment.infrastructure.in.rest.dto.AppointmentDTOs.RescheduleAppointmentRequest;
import co.unicauca.piedrazul.appointment.infrastructure.in.rest.mapper.AppointmentDtoMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.security.access.prepost.PreAuthorize;
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
 * Adaptador REST de entrada para gestión de citas médicas.
 * Delega en el puerto AppointmentUseCase; las excepciones son traducidas por AppointmentExceptionHandler.
 */
@RestController
@RequestMapping("/api/v1/appointments")
@Tag(name = "Appointments", description = "Gestión de citas médicas")
public class AppointmentController {

    private final AppointmentUseCase appointmentUseCase;
    private final AppointmentDtoMapper appointmentMapper;

    public AppointmentController(AppointmentUseCase appointmentUseCase,
                                  AppointmentDtoMapper appointmentMapper) {
        this.appointmentUseCase = appointmentUseCase;
        this.appointmentMapper = appointmentMapper;
    }

    @GetMapping("/doctor/{doctorId}/date/{date}")
    @Operation(summary = "Listar citas por médico y fecha")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENDADOR', 'DOCTOR', 'PACIENTE')")
    public ResponseEntity<List<AppointmentResponse>> listByDoctorAndDate(
            @PathVariable long doctorId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(
                appointmentUseCase.listByDoctorAndDate(doctorId, date)
                        .stream().map(appointmentMapper::toResponse).toList());
    }

    @PostMapping
    @Operation(summary = "Crear cita manual")
    @PreAuthorize("hasAnyRole('AGENDADOR', 'PACIENTE')")
    public ResponseEntity<AppointmentResponse> createAppointment(
            @Valid @RequestBody CreateAppointmentRequest request) {
        Appointment appointment = appointmentMapper.toEntity(request);
        Appointment saved = appointmentUseCase.scheduleAppointment(appointment);
        return ResponseEntity.status(HttpStatus.CREATED).body(appointmentMapper.toResponse(saved));
    }

    @PatchMapping("/{id}/reschedule")
    @Operation(summary = "Reagendar cita")
    @PreAuthorize("hasAnyRole('AGENDADOR', 'DOCTOR')")
    public ResponseEntity<AppointmentResponse> reschedule(
            @PathVariable int id,
            @Valid @RequestBody RescheduleAppointmentRequest request) {
        Appointment updated = appointmentUseCase.rescheduleAppointment(
                id, request.newDoctorId(), request.doctorName(), request.serviceType(),
                request.newDate(), request.newStartTime(), request.newEndTime());
        return ResponseEntity.ok(appointmentMapper.toResponse(updated));
    }

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Cancelar cita")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENDADOR', 'PACIENTE', 'DOCTOR')")
    public ResponseEntity<AppointmentResponse> cancel(@PathVariable int id) {
        return ResponseEntity.ok(appointmentMapper.toResponse(appointmentUseCase.cancelAppointment(id)));
    }

    @PatchMapping("/{id}/attend")
    @Operation(summary = "Marcar cita como atendida y otorgar autorización de servicio opcional")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<AppointmentResponse> markAsAttended(
            @PathVariable int id,
            @RequestBody(required = false) @Nullable AttendAppointmentRequest request) {
        ServiceType authorized = (request != null) ? request.authorizedServiceType() : null;
        return ResponseEntity.ok(
                appointmentMapper.toResponse(appointmentUseCase.markAsAttended(id, authorized)));
    }

    @GetMapping("/patient/{patientId}/authorization")
    @Operation(summary = "Obtener autorización de servicio activa del paciente")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENDADOR', 'PACIENTE', 'DOCTOR')")
    public ResponseEntity<?> getPatientAuthorization(@PathVariable long patientId) {
        return appointmentUseCase.getPatientAuthorization(patientId)
                .<ResponseEntity<?>>map(auth -> ResponseEntity.ok(new PatientAuthorizationResponse(
                        auth.getAuthId(),
                        auth.getServiceType(),
                        auth.getAuthorizedAt(),
                        auth.getExpiresAt())))
                .orElse(ResponseEntity.noContent().build());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar cita por ID")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENDADOR', 'PACIENTE')")
    public ResponseEntity<AppointmentResponse> findById(@PathVariable int id) {
        return ResponseEntity.ok(appointmentMapper.toResponse(appointmentUseCase.findById(id)));
    }

    @GetMapping
    @Operation(summary = "Listar todas las citas")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENDADOR')")
    public ResponseEntity<List<AppointmentResponse>> listAll() {
        return ResponseEntity.ok(
                appointmentUseCase.listAll().stream().map(appointmentMapper::toResponse).toList());
    }

    @GetMapping("/doctor/{doctorId}/all")
    @Operation(summary = "Listar todas las citas de un médico (sin filtro de fecha)")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENDADOR', 'DOCTOR')")
    public ResponseEntity<List<AppointmentResponse>> listByDoctor(@PathVariable long doctorId) {
        return ResponseEntity.ok(
                appointmentUseCase.listByDoctor(doctorId)
                        .stream().map(appointmentMapper::toResponse).toList());
    }

    @GetMapping("/patient/{patientId}")
    @Operation(summary = "Listar citas de un paciente")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENDADOR', 'PACIENTE')")
    public ResponseEntity<List<AppointmentResponse>> listByPatient(@PathVariable long patientId) {
        return ResponseEntity.ok(
                appointmentUseCase.listByPatient(patientId)
                        .stream().map(appointmentMapper::toResponse).toList());
    }
}
