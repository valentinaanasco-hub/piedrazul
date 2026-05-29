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
    
    // Exchange y cola para eventos de citas
    public static final String APPOINTMENT_EXCHANGE = "piedrazul.appointment.exchange";
    public static final String QUEUE_APPOINTMENT_CREATED = "piedrazul.appointment.created.queue";
    public static final String ROUTING_KEY_APPOINTMENT_CREATED = "appointment.created";

    // Exchange y colas para eventos de configuración
    public static final String CONFIGURATION_EXCHANGE = "piedrazul.configuration.exchange";
    public static final String QUEUE_SCHEDULE_UPDATED = "piedrazul.schedule.updated.queue";
    public static final String ROUTING_KEY_SCHEDULE_UPDATED = "schedule.updated";
    public static final String QUEUE_GLOBAL_CONFIG_UPDATED = "piedrazul.global.config.updated.queue";
    public static final String ROUTING_KEY_GLOBAL_CONFIG_UPDATED = "global.config.updated";

    // ========== APPOINTMENT EXCHANGE ==========

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

    // ========== CONFIGURATION EXCHANGE ==========

    @Bean
    public TopicExchange configurationExchange() {
        return new TopicExchange(CONFIGURATION_EXCHANGE);
    }

    @Bean
    public Queue scheduleUpdatedQueue() {
        return new Queue(QUEUE_SCHEDULE_UPDATED, true);
    }

    @Bean
    public Binding scheduleUpdatedBinding(Queue scheduleUpdatedQueue, TopicExchange configurationExchange) {
        return BindingBuilder.bind(scheduleUpdatedQueue)
                             .to(configurationExchange)
                             .with(ROUTING_KEY_SCHEDULE_UPDATED);
    }

    @Bean
    public Queue globalConfigUpdatedQueue() {
        return new Queue(QUEUE_GLOBAL_CONFIG_UPDATED, true);
    }

    @Bean
    public Binding globalConfigUpdatedBinding(Queue globalConfigUpdatedQueue, TopicExchange configurationExchange) {
        return BindingBuilder.bind(globalConfigUpdatedQueue)
                             .to(configurationExchange)
                             .with(ROUTING_KEY_GLOBAL_CONFIG_UPDATED);
    }

    // ========== MESSAGE CONVERTER ==========

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
