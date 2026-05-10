package co.unicauca.piedrazul.appointment.domain.service;

import co.unicauca.piedrazul.appointment.domain.entities.Appointment;
import co.unicauca.piedrazul.appointment.domain.entities.enums.AppointmentStatus;
import co.unicauca.piedrazul.appointment.domain.repository.AppointmentRepository;
import co.unicauca.piedrazul.appointment.domain.validator.AppointmentValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Servicio de dominio para gestión de citas médicas.
 *
 * Open/Closed: validaciones inyectadas como lista de AppointmentValidator.
 * Para agregar una regla nueva, crear @Component que implemente AppointmentValidator.
 *
 * @author Valentina Añasco
 * @author Camila Dorado
 * @author Felipe Gutierrez
 * @author Ginner Ortega
 * @author Santiago Solarte
 */
@Service
public class AppointmentService implements IAppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final List<AppointmentValidator> validators;

    public AppointmentService(AppointmentRepository appointmentRepository,
                               List<AppointmentValidator> validators) {
        this.appointmentRepository = appointmentRepository;
        this.validators = validators;
    }

    @Override
    @Transactional
    public Appointment scheduleAppointment(Appointment appointment) {
        List<Appointment> existingOnDate = getActiveAppointmentsByDoctorAndDate(
                appointment.getDoctorId(), appointment.getDate());
        runValidators(appointment, existingOnDate);
        appointment.setStatus(AppointmentStatus.AGENDADA);
        return appointmentRepository.save(appointment);
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

        List<Appointment> existingOnDate = getActiveAppointmentsByDoctorAndDate(
                appointment.getDoctorId(), newDate);
        runValidators(appointment, existingOnDate);

        appointment.setStatus(AppointmentStatus.REAGENDADA);
        return appointmentRepository.save(appointment);
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

    public List<Appointment> getActiveAppointmentsByDoctorAndDate(int doctorId, LocalDate date) {
        return appointmentRepository.findByDoctorIdAndDateAndStatusNot(
                doctorId, date, AppointmentStatus.CANCELADA);
    }

    private void runValidators(Appointment appointment, List<Appointment> existingOnDate) {
        for (AppointmentValidator validator : validators) {
            validator.validate(appointment, existingOnDate);
        }
    }
}
