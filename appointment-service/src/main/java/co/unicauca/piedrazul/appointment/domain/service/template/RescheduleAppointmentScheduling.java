package co.unicauca.piedrazul.appointment.domain.service.template;

import co.unicauca.piedrazul.appointment.domain.model.Appointment;
import co.unicauca.piedrazul.appointment.domain.port.out.AppointmentEventPort;
import co.unicauca.piedrazul.appointment.domain.port.out.AppointmentRepositoryPort;
import co.unicauca.piedrazul.appointment.domain.service.validator.AppointmentValidator;

import java.util.List;

/**
 * Subclase concreta del Template Method para reagendamiento de citas.
 * Estado asignado: REAGENDADA — mediante el método de dominio markRescheduled().
 * Clase pura de dominio — registrada como @Bean en AppConfig.
 */
public class RescheduleAppointmentScheduling extends AppointmentSchedulingTemplate {

    public RescheduleAppointmentScheduling(AppointmentRepositoryPort repositoryPort,
                                            List<AppointmentValidator> validators,
                                            AppointmentEventPort eventPort) {
        super(repositoryPort, validators, eventPort);
    }

    @Override
    protected void assignStatus(Appointment appointment) {
        appointment.markRescheduled();
    }
}
