package co.unicauca.piedrazul.appointment.infrastructure.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de RabbitMQ para el appointment-service.
 * Vive en infraestructura — el dominio no conoce estos beans.
 */
@Configuration
public class RabbitMQConfig {

    public static final String APPOINTMENT_EXCHANGE            = "piedrazul.exchange";
    public static final String ROUTING_KEY_APPOINTMENT_CREATED = "appointment.created";
    public static final String QUEUE_USER_REGISTERED           = "appointment.user.registered";

    @Bean
    public TopicExchange appointmentExchange() {
        return new TopicExchange(APPOINTMENT_EXCHANGE);
    }

    @Bean
    public Queue userRegisteredQueue() {
        return new Queue(QUEUE_USER_REGISTERED, true);
    }

    @Bean
    public Binding userRegisteredBinding(Queue userRegisteredQueue,
                                          TopicExchange appointmentExchange) {
        return BindingBuilder.bind(userRegisteredQueue)
                .to(appointmentExchange)
                .with("user.registered");
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}
