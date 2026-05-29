package co.unicauca.piedrazul.configuration.application;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de RabbitMQ para configuration-service.
 * Publica eventos de configuración que son consumidos por otros servicios.
 *
 * @author Santiago Solarte
 */
@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "piedrazul.configuration.exchange";
    
    // Configuración de horarios
    public static final String QUEUE_SCHEDULE_UPDATED = "piedrazul.schedule.updated.queue";
    public static final String ROUTING_KEY_SCHEDULE_UPDATED = "schedule.updated";
    
    // Configuración global
    public static final String QUEUE_GLOBAL_CONFIG_UPDATED = "piedrazul.global.config.updated.queue";
    public static final String ROUTING_KEY_GLOBAL_CONFIG_UPDATED = "global.config.updated";

    @Bean
    public TopicExchange configurationExchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Queue scheduleUpdatedQueue() {
        return new Queue(QUEUE_SCHEDULE_UPDATED, true);
    }

    @Bean
    public Binding scheduleUpdatedBinding(Queue scheduleUpdatedQueue,
                                           TopicExchange configurationExchange) {
        return BindingBuilder
                .bind(scheduleUpdatedQueue)
                .to(configurationExchange)
                .with(ROUTING_KEY_SCHEDULE_UPDATED);
    }

    @Bean
    public Queue globalConfigUpdatedQueue() {
        return new Queue(QUEUE_GLOBAL_CONFIG_UPDATED, true);
    }

    @Bean
    public Binding globalConfigUpdatedBinding(Queue globalConfigUpdatedQueue,
                                                TopicExchange configurationExchange) {
        return BindingBuilder
                .bind(globalConfigUpdatedQueue)
                .to(configurationExchange)
                .with(ROUTING_KEY_GLOBAL_CONFIG_UPDATED);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}
