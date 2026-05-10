package co.unicauca.piedrazul.appointment.application;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de RabbitMQ para appointment-service
 * Se suscribe al exchange de identity-service para recibir eventos de usuario
 */
@Configuration
public class RabbitMQConfig {

    public static final String IDENTITY_EXCHANGE = "piedrazul.identity.exchange";
    public static final String QUEUE_USER_REGISTERED = "piedrazul.user.registered.queue";
    public static final String ROUTING_KEY_USER_REGISTERED = "user.registered";

    @Bean
    public TopicExchange identityExchange() {
        return new TopicExchange(IDENTITY_EXCHANGE);
    }

    @Bean
    public Queue userRegisteredQueue() {
        return new Queue(QUEUE_USER_REGISTERED, true);
    }

    @Bean
    public Binding userRegisteredBinding(Queue userRegisteredQueue,
                                          TopicExchange identityExchange) {
        return BindingBuilder
                .bind(userRegisteredQueue)
                .to(identityExchange)
                .with(ROUTING_KEY_USER_REGISTERED);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
