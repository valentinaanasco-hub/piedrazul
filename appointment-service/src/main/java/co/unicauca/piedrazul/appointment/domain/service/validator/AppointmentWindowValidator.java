package co.unicauca.piedrazul.appointment.domain.service.validator;

import co.unicauca.piedrazul.appointment.domain.exception.AppointmentValidationException;
import co.unicauca.piedrazul.appointment.domain.model.Appointment;
import co.unicauca.piedrazul.appointment.domain.port.out.AppointmentWindowPort;

import java.time.LocalDate;
import java.util.List;

/**
 * Valida que la fecha de la cita esté dentro de la ventana de tiempo
 * configurada por el administrador (en semanas desde hoy).
 * Clase pura de dominio — registrada como @Bean en AppConfig.
 */
public class AppointmentWindowValidator implements AppointmentValidator {

    private final AppointmentWindowPort windowPort;

    public AppointmentWindowValidator(AppointmentWindowPort windowPort) {
        this.windowPort = windowPort;
    }

    @Override
    public void validate(Appointment appointment, List<Appointment> existingOnDate) {
        int weeks = windowPort.getAppointmentWindowWeeks();
        LocalDate maxDate = LocalDate.now().plusWeeks(weeks);

        if (appointment.getDate().isAfter(maxDate)) {
            throw new AppointmentValidationException(
                    "Solo puedes agendar citas con hasta " + weeks +
                    " semana(s) de anticipación. Fecha máxima: " + maxDate);
        }
    }
}
