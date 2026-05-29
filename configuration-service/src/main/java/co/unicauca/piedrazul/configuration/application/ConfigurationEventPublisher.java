package co.unicauca.piedrazul.configuration.application;

import co.unicauca.piedrazul.configuration.domain.entities.DoctorScheduleConfiguration;
import co.unicauca.piedrazul.events.GlobalConfigurationUpdatedEvent;
import co.unicauca.piedrazul.events.ScheduleConfigurationUpdatedEvent;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Publica eventos de configuración en RabbitMQ.
 * Usado por ConfigurationService después de actualizar configuraciones.
 *
 * @author Santiago Solarte
 */
@Component
public class ConfigurationEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public ConfigurationEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * Publica un evento cuando se actualiza la configuración de horarios de un profesional.
     */
    public void publishScheduleUpdated(int doctorId, List<DoctorScheduleConfiguration> schedules) {
        List<ScheduleConfigurationUpdatedEvent.ScheduleItem> items = schedules.stream()
                .map(schedule -> new ScheduleConfigurationUpdatedEvent.ScheduleItem(
                        schedule.getDayOfWeek(),
                        schedule.getStartTime(),
                        schedule.getEndTime(),
                        schedule.getIntervalMinutes()
                ))
                .toList();

        ScheduleConfigurationUpdatedEvent event = new ScheduleConfigurationUpdatedEvent(
                doctorId,
                items
        );

        System.out.println("[CONFIG-PUBLISHER] Publicando evento de horarios actualizados para doctor: " + doctorId + 
                         " con " + items.size() + " días configurados");

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE,
                RabbitMQConfig.ROUTING_KEY_SCHEDULE_UPDATED,
                event
        );
        
        System.out.println("[CONFIG-PUBLISHER] Evento publicado exitosamente");
    }

    /**
     * Publica un evento cuando se actualiza un parámetro global del sistema.
     */
    public void publishGlobalConfigUpdated(String key, String value) {
        GlobalConfigurationUpdatedEvent event = new GlobalConfigurationUpdatedEvent(key, value);

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE,
                RabbitMQConfig.ROUTING_KEY_GLOBAL_CONFIG_UPDATED,
                event
        );
    }
}
