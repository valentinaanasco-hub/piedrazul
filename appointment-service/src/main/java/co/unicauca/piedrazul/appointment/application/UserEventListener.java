package co.unicauca.piedrazul.appointment.application;

import co.unicauca.piedrazul.appointment.domain.entities.UserCache;
import co.unicauca.piedrazul.appointment.domain.repository.UserCacheRepository;
import co.unicauca.piedrazul.events.UserRegisteredEvent;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Escucha eventos de usuario publicados por identity-service
 * Mantiene la caché local de usuarios actualizada para validar citas
 * sin necesidad de llamadas HTTP entre servicios
 */
@Component
public class UserEventListener {

    private final UserCacheRepository userCacheRepository;

    public UserEventListener(UserCacheRepository userCacheRepository) {
        this.userCacheRepository = userCacheRepository;
    }

    /**
     * Recibe el evento y persiste en la caché local
     * Si el usuario ya existe lo actualiza, si no lo crea
     */
    @RabbitListener(queues = RabbitMQConfig.QUEUE_USER_REGISTERED)
    public void onUserRegistered(UserRegisteredEvent event) {
        UserCache cache = userCacheRepository.findById(event.userId())
                .orElse(new UserCache());

        cache.setUserId(event.userId());
        cache.setFullName(event.fullName());
        cache.setRole(event.role());
        cache.setState(event.state());

        userCacheRepository.save(cache);
    }
}
