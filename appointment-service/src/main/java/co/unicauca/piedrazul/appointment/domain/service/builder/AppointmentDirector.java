package co.unicauca.piedrazul.appointment.domain.service.builder;

import co.unicauca.piedrazul.appointment.domain.model.Appointment;
import co.unicauca.piedrazul.appointment.domain.model.ServiceType;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Director del patrón Builder para Appointment.
 * Orquesta el orden de los pasos según el tipo de cita.
 * Es stateless: crea un AppointmentBuilder nuevo por cada llamada.
 *
 * Tipos soportados:
 *  - buildManualAppointment    : agendada por el agendador con todos los campos
 *  - buildWebAppointment       : agendada por el paciente desde la web
 *  - buildAutonomousAppointment: asignada automáticamente por el sistema
 */
public class AppointmentDirector {

    /**
     * Cita creada manualmente por el agendador.
     * Todos los campos son relevantes, incluyendo reason y notes.
     */
    public Appointment buildManualAppointment(long doctorId, String doctorName, long patientId,
                                              LocalDate date, LocalTime startTime, LocalTime endTime,
                                              String reason, String notes, ServiceType serviceType) {
        return new AppointmentBuilder()
                .doctorId(doctorId)
                .doctorName(doctorName)
                .patientId(patientId)
                .date(date)
                .startTime(startTime)
                .endTime(endTime)
                .reason(reason != null && !reason.isBlank() ? reason : "Sin especificar")
                .notes(notes)
                .serviceType(serviceType != null ? serviceType : ServiceType.CONSULTA_GENERAL)
                .build();
    }

    /**
     * Cita agendada por el paciente desde la web.
     * El reason se fija automáticamente; no incluye notas.
     */
    public Appointment buildWebAppointment(long doctorId, String doctorName, long patientId,
                                           LocalDate date, LocalTime startTime, LocalTime endTime, ServiceType serviceType) {
        return new AppointmentBuilder()
                .doctorId(doctorId)
                .doctorName(doctorName)
                .patientId(patientId)
                .date(date)
                .startTime(startTime)
                .endTime(endTime)
                .reason("Cita agendada por paciente")
                .serviceType(serviceType)
                .build();
    }

    /**
     * Cita asignada automáticamente por el sistema de agendamiento autónomo.
     * El slot (doctor, fecha y hora) proviene del algoritmo de disponibilidad.
     * No requiere reason ni notes del usuario.
     */
    public Appointment buildAutonomousAppointment(long doctorId, String doctorName, long patientId,
                                                   LocalDate date, LocalTime startTime, LocalTime endTime, ServiceType serviceType) {
        return new AppointmentBuilder()
                .doctorId(doctorId)
                .doctorName(doctorName)
                .patientId(patientId)
                .date(date)
                .startTime(startTime)
                .endTime(endTime)
                .reason("Cita asignada automáticamente por el sistema")
                .notes("Agendamiento autónomo")
                .serviceType(serviceType)
                .build();
    }
}
