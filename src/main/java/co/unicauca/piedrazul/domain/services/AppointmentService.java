/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package co.unicauca.piedrazul.domain.services;

import co.unicauca.piedrazul.domain.entities.Appointment;
import co.unicauca.piedrazul.domain.entities.Doctor;
import co.unicauca.piedrazul.domain.entities.Patient;
import java.util.List;
import co.unicauca.piedrazul.domain.acces.IAppointmentRepository;
import co.unicauca.piedrazul.domain.acces.IDoctorRepository;
import co.unicauca.piedrazul.domain.acces.IPatientRepository;

/**
 * @author Valentina Añasco 
 * @author Camila Dorado
 * @author Felipe Gutierrez
 * @author Ginner Ortega
 * @author Santiago Solarte 
 */

// Servicio que maneja toda la lógica de negocio de citas
public class AppointmentService {
  

    private final IAppointmentRepository appointmentRepository;
    private final IDoctorRepository doctorRepository;
    private final IPatientRepository patientRepository;
    private final AppointmentValidator validator = new AppointmentValidator();

    public AppointmentService(IAppointmentRepository appointmentRepository,
                              IDoctorRepository doctorRepository,
                              IPatientRepository patientRepository){

        // 1. Inyección de dependencias (DIP)
        this.appointmentRepository = appointmentRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
    }

    public boolean scheduleAppointment(Appointment appointment) {

        // 2. Validar fecha
        validator.validateDate(appointment.getDate());

        // 3. Validar doctor
        Doctor doctor = doctorRepository.findById(appointment.getDoctor().getId());
        validator.validateDoctor(doctor);

        // 4. Validar paciente
        Patient patient = patientRepository.findById(appointment.getPatient().getId());
        validator.validatePatient(patient);

        // 5. Validar que la hora de inicio sea antes de la final
        if (appointment.getStartTime().isAfter(appointment.getEndTime())) {
            throw new IllegalArgumentException("La hora de inicio debe ser menor que la hora final");
        }

        // 6. Validar conflicto de horario del médico
        validateTimeConflict(appointment);

        // 7. Asignar estado inicial
        appointment.setStatus("REAGENDADA");

        // 8. Guardar la cita
        return appointmentRepository.save(appointment);
    }

    public Appointment findAppointment(int id) {

        // 9. Buscar cita por id
        Appointment appointment = appointmentRepository.findById(id);

        // 10. Validar que exista
        if (appointment == null)
            throw new IllegalArgumentException("Cita no encontrada");

        return appointment;
    }

    public List<Appointment> listAppointments() {
        // 11. Retorna todas las citas
        return appointmentRepository.findAll();
    }

    public boolean rescheduleAppointment(Appointment appointment) {

        // 12. Verificar que la cita exista
        Appointment existing = appointmentRepository.findById(appointment.getAppointmentId());
        if (existing == null)
            throw new IllegalArgumentException("Cita no encontrada");

        // 13. Validar nueva fecha
        validator.validateDate(appointment.getDate());

        // 14. Validar horas
        if (appointment.getStartTime().isAfter(appointment.getEndTime())) {
            throw new IllegalArgumentException("La hora de inicio debe ser menor que la hora final");
        }

        // 15. Validar conflicto de horario
        validateTimeConflict(appointment);

        // 16. Actualizar cita
        return appointmentRepository.update(appointment);
    }

    public boolean cancelAppointment(int id) {

        // 17. Buscar cita
        Appointment appointment = findAppointment(id);

        // 18. Cambiar estado a CANCELADA
        appointment.setStatus("CANCELADA");

        return appointmentRepository.update(appointment);
    }

    public boolean markAsAttended(int id) {

        // 19. Buscar cita
        Appointment appointment = findAppointment(id);

        // 20. Cambiar estado a ATENDIDA
        appointment.setStatus("ATENDIDA");

        return appointmentRepository.update(appointment);
    }

    // valida que no haya cruce de horarios
    private void validateTimeConflict(Appointment appointment) {

        // 21. Obtener citas del mismo médico en esa fecha
        List<Appointment> appointments = appointmentRepository
                .findByDoctorAndDate(
                        appointment.getDoctor().getId(),
                        appointment.getDate().toString()
                );

        // 22. Recorrer citas existentes
        for (Appointment a : appointments) {

            // 23. Ignorar la misma cita (en caso de edición)
            if (a.getAppointmentId() == appointment.getAppointmentId()) {
                continue;
            }

            // 24. Validar cruce de horarios
            if (appointment.getStartTime().isBefore(a.getEndTime()) &&
                appointment.getEndTime().isAfter(a.getStartTime())) {

                throw new IllegalArgumentException(
                        "El médico ya tiene una cita en ese horario"
                );
            }
        }
    }
}
