package co.unicauca.piedrazul.medical.application;

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

@Configuration
public class RabbitMQConfig {
    
    public static final String APPOINTMENT_EXCHANGE = "piedrazul.appointment.exchange";
    public static final String QUEUE_APPOINTMENT_CREATED = "piedrazul.appointment.created.queue";
    public static final String ROUTING_KEY_APPOINTMENT_CREATED = "appointment.created";

    @Bean 
    public TopicExchange appointmentExchange(){
        return new TopicExchange(APPOINTMENT_EXCHANGE);
    }

    @Bean
    public Queue appointmentCreatedQueue(){
        return new Queue(QUEUE_APPOINTMENT_CREATED, true);
    }

    @Bean
    public Binding appointmentCreatedBinding(Queue appointmentCreatedQueue, TopicExchange appointmentExchange){
        return BindingBuilder.bind(appointmentCreatedQueue)
                             .to(appointmentExchange)
                             .with(ROUTING_KEY_APPOINTMENT_CREATED);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory){
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
    
}
