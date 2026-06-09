package co.unicauca.piedrazul.appointment.application;

import co.unicauca.piedrazul.appointment.domain.model.UserCache;
import co.unicauca.piedrazul.appointment.domain.port.out.UserValidationPort;
import co.unicauca.piedrazul.events.UserRegisteredEvent;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Escucha eventos de usuario publicados por identity-service.
 * Mantiene la caché local actualizada usando el puerto de salida UserValidationPort.
 */
@Component
public class UserEventListener {

    private final UserValidationPort userValidationPort;

    public UserEventListener(UserValidationPort userValidationPort) {
        this.userValidationPort = userValidationPort;
    }

    @RabbitListener(queues = "${rabbitmq.queue.user-registered}")
    public void onUserRegistered(UserRegisteredEvent event) {
        UserCache user = new UserCache();
        user.setUserId(event.userId());
        user.setFullName(event.fullName());
        user.setRole(event.role());
        user.setState(event.state());
        userValidationPort.saveUser(user);
    }
}
