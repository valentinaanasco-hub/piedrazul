package co.unicauca.piedrazul.identity.application;

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
 * Configuración de RabbitMQ para identity-service.
 * Define el exchange, colas y bindings para eventos de usuario.
 */
@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "piedrazul.identity.exchange";
    public static final String QUEUE_USER_REGISTERED = "piedrazul.user.registered.queue";
    public static final String ROUTING_KEY_USER_REGISTERED = "user.registered";

    @Bean
    public TopicExchange identityExchange() {
        return new TopicExchange(EXCHANGE);
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

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}
