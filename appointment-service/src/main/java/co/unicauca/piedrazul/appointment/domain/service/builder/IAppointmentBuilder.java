package co.unicauca.piedrazul.appointment.domain.service.builder;

import co.unicauca.piedrazul.appointment.domain.model.Appointment;
import co.unicauca.piedrazul.appointment.domain.model.ServiceType;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Interfaz del patrón Builder para construir objetos Appointment del dominio.
 */
public interface IAppointmentBuilder {

    IAppointmentBuilder doctorId(long doctorId);
    IAppointmentBuilder doctorName(String doctorName);
    IAppointmentBuilder patientId(long patientId);
    IAppointmentBuilder date(LocalDate date);
    IAppointmentBuilder startTime(LocalTime startTime);
    IAppointmentBuilder endTime(LocalTime endTime);
    IAppointmentBuilder reason(String reason);
    IAppointmentBuilder notes(String notes);
    IAppointmentBuilder serviceType(ServiceType serviceType);
    Appointment build();
}
