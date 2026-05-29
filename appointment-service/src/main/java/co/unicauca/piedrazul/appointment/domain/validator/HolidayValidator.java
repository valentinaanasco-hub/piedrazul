package co.unicauca.piedrazul.appointment.domain.validator;

import co.unicauca.piedrazul.appointment.domain.entities.Appointment;
import co.unicauca.piedrazul.appointment.domain.service.ColombianHolidaysService;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Valida que no se agenden citas en festivos colombianos
 */
@Component
public class HolidayValidator implements AppointmentValidator {

    private final ColombianHolidaysService holidaysService;

    public HolidayValidator(ColombianHolidaysService holidaysService) {
        this.holidaysService = holidaysService;
    }

    @Override
    public void validate(Appointment appointment, List<Appointment> existingOnDate) {
        if (holidaysService.isHoliday(appointment.getDate())) {
            throw new IllegalArgumentException(
                    "No se pueden agendar citas en días festivos. Por favor selecciona otra fecha");
        }
    }
}
