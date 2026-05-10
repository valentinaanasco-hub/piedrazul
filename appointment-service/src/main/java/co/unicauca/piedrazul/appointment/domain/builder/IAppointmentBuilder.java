package co.unicauca.piedrazul.appointment.domain.builder;

import co.unicauca.piedrazul.appointment.domain.entities.Appointment;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Interfaz del patrón Builder para construir objetos Appointment
 * Define los pasos de construcción que el ConcreteBuilder debe implementar
 */
public interface IAppointmentBuilder {

    IAppointmentBuilder doctorId(int doctorId);

    IAppointmentBuilder patientId(int patientId);

    IAppointmentBuilder date(LocalDate date);

    IAppointmentBuilder startTime(LocalTime startTime);

    IAppointmentBuilder endTime(LocalTime endTime);

    IAppointmentBuilder reason(String reason);

    IAppointmentBuilder notes(String notes);

    Appointment build();
}
