package co.unicauca.piedrazul.appointment.domain.template;

import co.unicauca.piedrazul.appointment.domain.entities.Appointment;
import co.unicauca.piedrazul.appointment.domain.entities.enums.AppointmentStatus;
import co.unicauca.piedrazul.appointment.domain.repository.AppointmentRepository;
import co.unicauca.piedrazul.appointment.domain.validator.AppointmentValidator;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Subclase concreta del Template Method para citas agendadas manualmente por el agendador
 * El estado asignado es AGENDADA
 */
@Component
public class ManualAppointmentScheduling extends AppointmentSchedulingTemplate {

    public ManualAppointmentScheduling(AppointmentRepository appointmentRepository,
                                        List<AppointmentValidator> validators) {
        super(appointmentRepository, validators);
    }

    @Override
    protected void assignStatus(Appointment appointment) {
        appointment.setStatus(AppointmentStatus.AGENDADA);
    }
}
