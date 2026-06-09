package co.unicauca.piedrazul.appointment.domain.service.validator;

import co.unicauca.piedrazul.appointment.domain.exception.AppointmentValidationException;
import co.unicauca.piedrazul.appointment.domain.model.Appointment;
import co.unicauca.piedrazul.appointment.domain.service.ColombianHolidaysService;

import java.util.List;

/**
 * Valida que no se agenden citas en festivos colombianos.
 * Clase pura de dominio — registrada como @Bean en AppConfig.
 */
public class HolidayValidator implements AppointmentValidator {

    private final ColombianHolidaysService holidaysService;

    public HolidayValidator(ColombianHolidaysService holidaysService) {
        this.holidaysService = holidaysService;
    }

    @Override
    public void validate(Appointment appointment, List<Appointment> existingOnDate) {
        if (holidaysService.isHoliday(appointment.getDate())) {
            throw new AppointmentValidationException(
                    "No se pueden agendar citas en días festivos. Por favor selecciona otra fecha");
        }
    }
}
