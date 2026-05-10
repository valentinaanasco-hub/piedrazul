package co.unicauca.piedrazul.appointment.domain.service;

import co.unicauca.piedrazul.appointment.domain.entities.Appointment;
import co.unicauca.piedrazul.appointment.domain.entities.enums.AppointmentStatus;
import co.unicauca.piedrazul.appointment.domain.repository.AppointmentRepository;
import co.unicauca.piedrazul.appointment.domain.template.ManualAppointmentScheduling;
import co.unicauca.piedrazul.appointment.domain.template.RescheduleAppointmentScheduling;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Servicio de dominio para gestión de citas médicas
 * Usa el patrón Template Method para el flujo de agendamiento y reagendamiento
 */
@Service
public class AppointmentService implements IAppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final ManualAppointmentScheduling manualScheduling;
    private final RescheduleAppointmentScheduling rescheduleScheduling;

    public AppointmentService(AppointmentRepository appointmentRepository,
                               ManualAppointmentScheduling manualScheduling,
                               RescheduleAppointmentScheduling rescheduleScheduling) {
        this.appointmentRepository = appointmentRepository;
        this.manualScheduling = manualScheduling;
        this.rescheduleScheduling = rescheduleScheduling;
    }

    @Override
    @Transactional
    public Appointment scheduleAppointment(Appointment appointment) {
        // Delega al Template Method — el esqueleto está en AppointmentSchedulingTemplate
        return manualScheduling.execute(appointment);
    }

    @Override
    public List<Appointment> listByDoctorAndDate(int doctorId, LocalDate date) {
        return appointmentRepository.findByDoctorIdAndDateOrderByStartTimeAsc(doctorId, date);
    }

    @Override
    @Transactional
    public Appointment rescheduleAppointment(int appointmentId, LocalDate newDate,
                                              LocalTime newStartTime, LocalTime newEndTime) {
        Appointment appointment = findById(appointmentId);
        appointment.setDate(newDate);
        appointment.setStartTime(newStartTime);
        appointment.setEndTime(newEndTime);
        // Delega al Template Method — el esqueleto está en AppointmentSchedulingTemplate
        return rescheduleScheduling.execute(appointment);
    }

    @Override
    @Transactional
    public Appointment cancelAppointment(int appointmentId) {
        Appointment appointment = findById(appointmentId);
        appointment.setStatus(AppointmentStatus.CANCELADA);
        return appointmentRepository.save(appointment);
    }

    @Override
    @Transactional
    public Appointment markAsAttended(int appointmentId) {
        Appointment appointment = findById(appointmentId);
        appointment.setStatus(AppointmentStatus.ATENDIDA);
        return appointmentRepository.save(appointment);
    }

    @Override
    public Appointment findById(int appointmentId) {
        return appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Cita no encontrada con ID: " + appointmentId));
    }

    @Override
    public List<Appointment> listAll() {
        return appointmentRepository.findAll();
    }

    @Override
    public List<Appointment> listByPatient(int patientId) {
        return appointmentRepository.findByPatientIdOrderByDateDescStartTimeAsc(patientId);
    }
}
