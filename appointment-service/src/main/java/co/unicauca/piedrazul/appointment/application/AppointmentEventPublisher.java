package co.unicauca.piedrazul.appointment.application;

import java.time.LocalDate;
import java.time.LocalTime;

import co.unicauca.piedrazul.events.AppointmentCreatedEvent;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import co.unicauca.piedrazul.appointment.domain.entities.Appointment;


@Component
public class AppointmentEventPublisher {
    
    private final RabbitTemplate rabbitTemplate;

    public AppointmentEventPublisher(RabbitTemplate rabbitTemplate){
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * Publica un evento cuando se registra un cita o su estado cambia.
     * 
     * @param appointment Cita creada o actualizada
     */
    public void publishAppointmentCreated(Appointment appointment){
        int appointmentId = appointment.getAppointmentId();
        int doctorId = appointment.getDoctorId();
        int patientId = appointment.getPatientId();
        LocalDate date = appointment.getDate();
        LocalTime startTime = appointment.getStartTime();
        LocalTime endTime = appointment.getEndTime();
        String status = appointment.getStatus().name();

        AppointmentCreatedEvent event = new AppointmentCreatedEvent(
            appointmentId,
            doctorId,
            patientId,
            date,
            startTime,
            endTime,
            status
        );

        rabbitTemplate.convertAndSend(RabbitMQConfig.APPOINTMENT_EXCHANGE
                                    , RabbitMQConfig.ROUTING_KEY_APPOINTMENT_CREATED
                                    , event
        );
    }

}
