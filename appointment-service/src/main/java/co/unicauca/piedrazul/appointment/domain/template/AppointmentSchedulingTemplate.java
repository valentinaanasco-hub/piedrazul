package co.unicauca.piedrazul.appointment.domain.template;

import co.unicauca.piedrazul.appointment.domain.entities.Appointment;
import co.unicauca.piedrazul.appointment.domain.repository.AppointmentRepository;
import co.unicauca.piedrazul.appointment.domain.validator.AppointmentValidator;

import java.util.List;

import co.unicauca.piedrazul.appointment.application.AppointmentEventPublisher;

/**
 * Clase abstracta del patrón Template Method para el flujo de agendamiento
 * Define el esqueleto del algoritmo: obtener existentes, validar, asignar estado y guardar
 * El paso variable es assignStatus(), que cada subclase implementa según el tipo de cita
 */
public abstract class AppointmentSchedulingTemplate {

    protected final AppointmentRepository appointmentRepository;
    protected final List<AppointmentValidator> validators;
    protected final AppointmentEventPublisher eventPublisher;

    protected AppointmentSchedulingTemplate(AppointmentRepository appointmentRepository,
                                             List<AppointmentValidator> validators,
                                            AppointmentEventPublisher eventPublisher) {
        this.appointmentRepository = appointmentRepository;
        this.validators = validators;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Esqueleto del algoritmo de agendamiento — no se puede sobreescribir
     * 1. Obtiene las citas activas del médico en esa fecha
     * 2. Ejecuta la cadena de validadores
     * 3. Asigna el estado (paso variable — lo define cada subclase)
     * 4. Guarda la cita
     */
    public final Appointment execute(Appointment appointment) {
        List<Appointment> existingOnDate = getActiveAppointments(appointment);
        runValidators(appointment, existingOnDate);
        assignStatus(appointment);
        Appointment saved = appointmentRepository.save(appointment);
        eventPublisher.publishAppointmentCreated(appointment);
        return saved;
    }

    /**
     * Paso variable — cada subclase define qué estado asignar
     */
    protected abstract void assignStatus(Appointment appointment);

    // --- Pasos fijos del algoritmo ---

    private List<Appointment> getActiveAppointments(Appointment appointment) {
        return appointmentRepository.findByDoctorIdAndDateForUpdate(
                appointment.getDoctorId(),
                appointment.getDate());
    }

    private void runValidators(Appointment appointment, List<Appointment> existingOnDate) {
        for (AppointmentValidator validator : validators) {
            validator.validate(appointment, existingOnDate);
        }
    }
}
