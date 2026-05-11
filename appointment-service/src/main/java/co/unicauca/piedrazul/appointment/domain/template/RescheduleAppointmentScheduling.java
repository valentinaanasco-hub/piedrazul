package co.unicauca.piedrazul.appointment.domain.template;

import co.unicauca.piedrazul.appointment.application.AppointmentEventPublisher;
import co.unicauca.piedrazul.appointment.domain.entities.Appointment;
import co.unicauca.piedrazul.appointment.domain.entities.enums.AppointmentStatus;
import co.unicauca.piedrazul.appointment.domain.repository.AppointmentRepository;
import co.unicauca.piedrazul.appointment.domain.validator.AppointmentValidator;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Subclase concreta del Template Method para reagendamiento de citas
 * El estado asignado es REAGENDADA
 */
@Component
public class RescheduleAppointmentScheduling extends AppointmentSchedulingTemplate {

    public RescheduleAppointmentScheduling(AppointmentRepository appointmentRepository,
                                            List<AppointmentValidator> validators,
                                        AppointmentEventPublisher eventPublisher) {
        super(appointmentRepository, validators, eventPublisher);
    }

    @Override
    protected void assignStatus(Appointment appointment) {
        appointment.setStatus(AppointmentStatus.REAGENDADA);
    }
}
