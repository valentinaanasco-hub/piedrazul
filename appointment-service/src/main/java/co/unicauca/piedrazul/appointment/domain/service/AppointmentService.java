package co.unicauca.piedrazul.appointment.domain.service;

import co.unicauca.piedrazul.appointment.domain.exception.AppointmentNotFoundException;
import co.unicauca.piedrazul.appointment.domain.model.Appointment;
import co.unicauca.piedrazul.appointment.domain.model.PatientAuthorization;
import co.unicauca.piedrazul.appointment.domain.model.ServiceType;
import co.unicauca.piedrazul.appointment.domain.port.in.AppointmentUseCase;
import co.unicauca.piedrazul.appointment.domain.port.out.AppointmentEventPort;
import co.unicauca.piedrazul.appointment.domain.port.out.AppointmentRepositoryPort;
import co.unicauca.piedrazul.appointment.domain.port.out.PatientAuthorizationPort;
import co.unicauca.piedrazul.appointment.domain.service.template.ManualAppointmentScheduling;
import co.unicauca.piedrazul.appointment.domain.service.template.RescheduleAppointmentScheduling;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * Servicio de aplicación que implementa el puerto de entrada AppointmentUseCase.
 * Orquesta los objetos de dominio; las invariantes de estado viven en el agregado Appointment.
 */
@Service
public class AppointmentService implements AppointmentUseCase {

    private final AppointmentRepositoryPort   repositoryPort;
    private final AppointmentEventPort        eventPort;
    private final ManualAppointmentScheduling manualScheduling;
    private final RescheduleAppointmentScheduling rescheduleScheduling;
    private final PatientAuthorizationPort    authorizationPort;

    public AppointmentService(AppointmentRepositoryPort repositoryPort,
                               AppointmentEventPort eventPort,
                               ManualAppointmentScheduling manualScheduling,
                               RescheduleAppointmentScheduling rescheduleScheduling,
                               PatientAuthorizationPort authorizationPort) {
        this.repositoryPort    = repositoryPort;
        this.eventPort         = eventPort;
        this.manualScheduling  = manualScheduling;
        this.rescheduleScheduling = rescheduleScheduling;
        this.authorizationPort = authorizationPort;
    }

    @Override
    @Transactional
    public Appointment scheduleAppointment(Appointment appointment) {
        Appointment saved = manualScheduling.execute(appointment);
        // Si el servicio es especializado, consumir la autorización activa del paciente
        if (saved.getServiceType() != ServiceType.CONSULTA_GENERAL) {
            authorizationPort.findActiveByPatientId(saved.getPatientId())
                    .ifPresent(auth -> authorizationPort.markAsUsed(auth.getAuthId()));
        }
        return saved;
    }

    @Override
    @Transactional
    public Appointment rescheduleAppointment(int appointmentId, Long newDoctorId, String doctorName,
                                              ServiceType serviceType, LocalDate newDate,
                                              LocalTime newStartTime, LocalTime newEndTime) {
        Appointment appointment = findById(appointmentId);
        if (!appointment.getDate().equals(LocalDate.now())) {
            throw new co.unicauca.piedrazul.appointment.domain.exception.AppointmentConflictException(
                    "Solo se puede reagendar una cita el mismo día en que está programada");
        }
        if (newDoctorId != null && newDoctorId > 0) {
            appointment.setDoctorId(newDoctorId);
        }
        if (doctorName != null && !doctorName.isBlank()) {
            appointment.setDoctorName(doctorName);
        }
        if (serviceType != null) {
            appointment.setServiceType(serviceType);
        }
        appointment.setDate(newDate);
        appointment.setStartTime(newStartTime);
        appointment.setEndTime(newEndTime);
        return rescheduleScheduling.execute(appointment);
    }

    @Override
    @Transactional
    public Appointment cancelAppointment(int appointmentId) {
        Appointment appointment = findById(appointmentId);
        appointment.cancel();
        Appointment saved = repositoryPort.save(appointment);
        eventPort.publishAppointmentEvent(saved);
        return saved;
    }

    @Override
    @Transactional
    public Appointment markAsAttended(int appointmentId, ServiceType authorizedServiceType) {
        Appointment appointment = findById(appointmentId);
        if (!appointment.getDate().equals(LocalDate.now())) {
            throw new co.unicauca.piedrazul.appointment.domain.exception.AppointmentValidationException(
                    "Solo se puede marcar como atendida una cita el mismo día en que está programada.");
        }
        appointment.markAsAttended();
        Appointment saved = repositoryPort.save(appointment);

        // Si el médico otorgó autorización para un servicio especializado, guardarla
        if (authorizedServiceType != null && authorizedServiceType != ServiceType.CONSULTA_GENERAL) {
            LocalDateTime now = LocalDateTime.now();
            PatientAuthorization auth = new PatientAuthorization(
                    saved.getPatientId(),
                    authorizedServiceType,
                    now,
                    now.plusMonths(1),
                    saved.getDoctorId(),
                    appointmentId
            );
            authorizationPort.save(auth);
        }

        eventPort.publishAppointmentEvent(saved);
        return saved;
    }

    @Override
    public Optional<PatientAuthorization> getPatientAuthorization(long patientId) {
        return authorizationPort.findActiveByPatientId(patientId);
    }

    @Override
    public Appointment findById(int appointmentId) {
        return repositoryPort.findById(appointmentId)
                .orElseThrow(() -> new AppointmentNotFoundException(
                        "Cita no encontrada con ID: " + appointmentId));
    }

    @Override
    public List<Appointment> listByDoctorAndDate(long doctorId, LocalDate date) {
        return repositoryPort.findByDoctorIdAndDate(doctorId, date);
    }

    @Override
    public List<Appointment> listAll() {
        return repositoryPort.findAll();
    }

    @Override
    public List<Appointment> listByDoctor(long doctorId) {
        return repositoryPort.findByDoctorId(doctorId);
    }

    @Override
    public List<Appointment> listByPatient(long patientId) {
        return repositoryPort.findByPatientIdOrderByDateDesc(patientId);
    }
}
