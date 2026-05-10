package co.unicauca.piedrazul.identity.application;

import co.unicauca.piedrazul.events.UserRegisteredEvent;
import co.unicauca.piedrazul.identity.domain.entities.User;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * Publica eventos de usuario en RabbitMQ.
 * Usado por IdentityService después de registrar o actualizar un usuario.
 */
@Component
public class UserEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public UserEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * Publica un evento cuando un usuario es registrado o su estado cambia.
     *
     * @param user El usuario registrado o actualizado.
     */
    public void publishUserRegistered(User user) {
        String fullName = buildFullName(user);
        String role = user.getRoles().isEmpty()
                ? "NO_ROLE"
                : user.getRoles().get(0).getRoleName();

        UserRegisteredEvent event = new UserRegisteredEvent(
                user.getId(),
                fullName,
                role,
                user.getState().name()
        );

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE,
                RabbitMQConfig.ROUTING_KEY_USER_REGISTERED,
                event
        );
    }

    private String buildFullName(User user) {
        StringBuilder sb = new StringBuilder();
        if (user.getFirstName() != null) sb.append(user.getFirstName()).append(" ");
        if (user.getFirstSurname() != null) sb.append(user.getFirstSurname());
        return sb.toString().trim();
    }
}
