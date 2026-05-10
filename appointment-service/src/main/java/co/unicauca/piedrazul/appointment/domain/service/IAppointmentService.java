package co.unicauca.piedrazul.appointment.domain.service;

import co.unicauca.piedrazul.appointment.domain.entities.Appointment;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Contrato del servicio de citas médicas.
 *
 * Interface Segregation: define solo los métodos que el controlador necesita.
 * Dependency Inversion: el controlador depende de esta abstracción.
 *
 * @author Santiago Solarte
 */
public interface IAppointmentService {

    Appointment scheduleAppointment(Appointment appointment);

    List<Appointment> listByDoctorAndDate(int doctorId, LocalDate date);

    Appointment rescheduleAppointment(int appointmentId, LocalDate newDate,
                                      LocalTime newStartTime, LocalTime newEndTime);

    Appointment cancelAppointment(int appointmentId);

    Appointment markAsAttended(int appointmentId);

    Appointment findById(int appointmentId);

    List<Appointment> listAll();

    List<Appointment> listByPatient(int patientId);
}
