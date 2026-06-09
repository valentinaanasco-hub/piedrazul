package co.unicauca.piedrazul.appointment.domain.port.in;

import co.unicauca.piedrazul.appointment.domain.model.Appointment;
import co.unicauca.piedrazul.appointment.domain.model.PatientAuthorization;
import co.unicauca.piedrazul.appointment.domain.model.ServiceType;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * Puerto de entrada: define los casos de uso disponibles para el dominio de citas.
 * Los adaptadores de entrada (REST) dependen de esta interfaz, no del servicio directamente.
 */
public interface AppointmentUseCase {

    Appointment scheduleAppointment(Appointment appointment);

    Appointment rescheduleAppointment(int appointmentId, Long newDoctorId, String doctorName,
                                      ServiceType serviceType, LocalDate newDate,
                                      LocalTime newStartTime, LocalTime newEndTime);

    Appointment cancelAppointment(int appointmentId);

    /**
     * Marca la cita como atendida y, opcionalmente, otorga al paciente
     * autorización para acceder a un tipo de servicio especializado en su próxima cita.
     *
     * @param appointmentId        ID de la cita
     * @param authorizedServiceType tipo de servicio autorizado (null = sin autorización extra)
     */
    Appointment markAsAttended(int appointmentId, ServiceType authorizedServiceType);

    /**
     * Devuelve la autorización de servicio activa (no usada, no expirada) del paciente,
     * si existe.
     */
    Optional<PatientAuthorization> getPatientAuthorization(long patientId);

    Appointment findById(int appointmentId);

    List<Appointment> listByDoctorAndDate(long doctorId, LocalDate date);

    List<Appointment> listAll();

    List<Appointment> listByDoctor(long doctorId);

    List<Appointment> listByPatient(long patientId);
}
