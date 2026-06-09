package co.unicauca.piedrazul.appointment.infrastructure.in.messaging;

import co.unicauca.piedrazul.appointment.domain.port.out.AppointmentWindowPort;
import co.unicauca.piedrazul.appointment.infrastructure.config.RabbitMQConfig;
import co.unicauca.piedrazul.events.GlobalConfigurationUpdatedEvent;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Adaptador de entrada (mensajería): escucha eventos de configuración global
 * publicados por configuration-service y actualiza el caché local.
 * Implementa AppointmentWindowPort para proveer el valor al dominio.
 */
@Component
public class GlobalConfigurationListener implements AppointmentWindowPort {

    private static final int DEFAULT_WINDOW_WEEKS = 4;
    private static final String APPOINTMENT_WINDOW_KEY = "appointment_window_weeks";

    private final AtomicInteger appointmentWindowWeeks = new AtomicInteger(DEFAULT_WINDOW_WEEKS);

    @RabbitListener(queues = RabbitMQConfig.QUEUE_GLOBAL_CONFIG)
    public void onGlobalConfigUpdated(GlobalConfigurationUpdatedEvent event) {
        if (APPOINTMENT_WINDOW_KEY.equals(event.parameterKey())) {
            try {
                int weeks = Integer.parseInt(event.parameterValue());
                appointmentWindowWeeks.set(weeks);
                System.out.println("[CONFIG-LISTENER] Ventana de citas actualizada: " + weeks + " semana(s)");
            } catch (NumberFormatException e) {
                System.err.println("[CONFIG-LISTENER] Valor inválido para ventana de citas: " + event.parameterValue());
            }
        }
    }

    @Override
    public int getAppointmentWindowWeeks() {
        return appointmentWindowWeeks.get();
    }
}
