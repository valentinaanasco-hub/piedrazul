package co.unicauca.piedrazul.appointment.domain.service.template;

import co.unicauca.piedrazul.appointment.domain.model.Appointment;
import co.unicauca.piedrazul.appointment.domain.port.out.AppointmentEventPort;
import co.unicauca.piedrazul.appointment.domain.port.out.AppointmentRepositoryPort;
import co.unicauca.piedrazul.appointment.domain.service.validator.AppointmentValidator;

import java.util.List;

/**
 * Clase abstracta del patrón Template Method para el flujo de agendamiento.
 * Define el esqueleto del algoritmo en execute() (final): obtener existentes,
 * validar, asignar estado y guardar. El paso variable es assignStatus().
 */
public abstract class AppointmentSchedulingTemplate {

    protected final AppointmentRepositoryPort repositoryPort;
    protected final List<AppointmentValidator> validators;
    protected final AppointmentEventPort eventPort;

    protected AppointmentSchedulingTemplate(AppointmentRepositoryPort repositoryPort,
                                             List<AppointmentValidator> validators,
                                             AppointmentEventPort eventPort) {
        this.repositoryPort = repositoryPort;
        this.validators = validators;
        this.eventPort = eventPort;
    }

    public final Appointment execute(Appointment appointment) {
        List<Appointment> existingOnDate = repositoryPort
                .findByDoctorIdAndDateWithLock(appointment.getDoctorId(), appointment.getDate());
        runValidators(appointment, existingOnDate);
        assignStatus(appointment);
        Appointment saved = repositoryPort.save(appointment);
        eventPort.publishAppointmentEvent(saved);
        return saved;
    }

    protected abstract void assignStatus(Appointment appointment);

    private void runValidators(Appointment appointment, List<Appointment> existingOnDate) {
        for (AppointmentValidator validator : validators) {
            validator.validate(appointment, existingOnDate);
        }
    }
}
