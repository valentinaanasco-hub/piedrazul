/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package co.unicauca.piedrazul.domain.services.interfaces;

import co.unicauca.piedrazul.domain.entities.Appointment;
import java.util.List;

/**
 *
 * @author santi
 */
public interface IAppointmentService {
    // Crea una nueva cita médica validando disponibilidad
    boolean scheduleAppointment(Appointment appointment);

    // Cambia la fecha u hora de una cita ya existente
    boolean rescheduleAppointment(Appointment appointment);

    // Marca una cita como CANCELADA para liberar el espacio
    boolean cancelAppointment(int id);

    // Registra que el paciente asistió a la cita
    boolean markAsAttended(int id);

    // Obtiene los detalles de una cita específica
    Appointment findAppointment(int id);

    // Lista el historial completo de citas registradas
    List<Appointment> listAppointments();
}
