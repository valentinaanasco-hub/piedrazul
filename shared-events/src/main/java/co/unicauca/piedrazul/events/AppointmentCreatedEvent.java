package co.unicauca.piedrazul.events;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Evento publicado por appointment-service cuando una cita es creada, modificada o cancelada
 * Consumido por medical-staff-service para mantener su caché local de slots ocupados
 *
 * @param appointmentId ID de la cita
 * @param doctorId      ID del médico
 * @param patientId     ID del paciente
 * @param date          Fecha de la cita
 * @param startTime     Hora de inicio
 * @param endTime       Hora de fin
 * @param status        Estado de la cita: AGENDADA, REAGENDADA, CANCELADA, ATENDIDA
 */
public record AppointmentCreatedEvent (
    int appointmentId, 
    int doctorId, 
    int patientId,
    LocalDate date, 
    LocalTime startTime, 
    LocalTime endTime,
    String status
){}
