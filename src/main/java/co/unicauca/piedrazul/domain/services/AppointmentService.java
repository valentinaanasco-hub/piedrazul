/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package co.unicauca.piedrazul.domain.services;

import co.unicauca.piedrazul.domain.acces.IAppointmentRepository;
import co.unicauca.piedrazul.domain.acces.IDoctorRepository;
import co.unicauca.piedrazul.domain.entities.Appointment;
import co.unicauca.piedrazul.domain.entities.Doctor;
import java.time.LocalDate;
import java.util.List;

/**
 * @author Valentina Añasco 
 * @author Camila Dorado
 * @author Felipe Gutierrez
 * @author Ginner Ortega
 * @author Santiago Solarte 
 */

public class AppointmentService {
    private final IAppointmentRepository appointmentRepository;
    private final IDoctorRepository doctorRepository;

    public AppointmentService(IAppointmentRepository appointmentRepository,
                               IDoctorRepository doctorRepository) {
        this.appointmentRepository = appointmentRepository;
        this.doctorRepository = doctorRepository;
    }

    public boolean scheduleAppointment(Appointment appointment) {
        // Valida que la fecha no sea nula
        if (appointment.getDate() == null)
            throw new IllegalArgumentException("La fecha es obligatoria");

        // Valida que la fecha no sea en el pasado
        if (appointment.getDate().isBefore(LocalDate.now()))
            throw new IllegalArgumentException("La fecha no puede ser en el pasado");

        // Valida que el médico exista y esté activo
        Doctor doctor = doctorRepository.findById(appointment.getDoctor().getId());
        if (doctor == null)
            throw new IllegalArgumentException("Médico no encontrado");
        if (doctor.getState().equals("INACTIVO"))
            throw new IllegalArgumentException("El médico no está activo");

        // Verifica que el horario exacto esté disponible
        Appointment existing = appointmentRepository.findByDoctorAndDateAndHour(
            appointment.getDoctor().getId(),
            appointment.getDate().toString(),
            appointment.getStartTime().toString(),
            appointment.getEndTime().toString()
        );

        // Si ya existe una cita en ese horario exacto, no se puede agendar
        if (existing != null)
            throw new IllegalArgumentException("El médico ya tiene una cita en ese horario");

        return appointmentRepository.save(appointment);
    }

    public Appointment findAppointment(int id) {
        Appointment appointment = appointmentRepository.findById(id);
        if (appointment == null)
            throw new IllegalArgumentException("Cita no encontrada");
        return appointment;
    }

    public List<Appointment> listAppointments() {
        return appointmentRepository.findAll();
    }

    public boolean rescheduleAppointment(Appointment appointment) {
        // Verifica que la cita exista antes de reagendarla
        Appointment existing = appointmentRepository.findById(appointment.getAppointmentId());
        if (existing == null)
            throw new IllegalArgumentException("Cita no encontrada");

        // Valida que la nueva fecha no sea en el pasado
        if (appointment.getDate().isBefore(LocalDate.now()))
            throw new IllegalArgumentException("La nueva fecha no puede ser en el pasado");

        // Verifica que el nuevo horario exacto esté disponible
        Appointment conflict = appointmentRepository.findByDoctorAndDateAndHour(
            appointment.getDoctor().getId(),
            appointment.getDate().toString(),
            appointment.getStartTime().toString(),
            appointment.getEndTime().toString()
        );

        // Permite reagendar si el conflicto es la misma cita
        if (conflict != null && conflict.getAppointmentId() != appointment.getAppointmentId())
            throw new IllegalArgumentException("El médico ya tiene una cita en ese horario");

        return appointmentRepository.update(appointment);
    }

    public boolean cancelAppointment(int id) {
        Appointment appointment = appointmentRepository.findById(id);
        if (appointment == null)
            throw new IllegalArgumentException("Cita no encontrada");

        // Cambia el estado a CANCELADA conservando el registro
        appointment.setStatus("CANCELADA");
        return appointmentRepository.update(appointment);
    }

    public boolean markAsAttended(int id) {
        Appointment appointment = appointmentRepository.findById(id);
        if (appointment == null)
            throw new IllegalArgumentException("Cita no encontrada");

        // Marca la cita como atendida al finalizar la consulta
        appointment.setStatus("ATENDIDA");
        return appointmentRepository.update(appointment);
    }
}
