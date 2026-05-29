package co.unicauca.piedrazul.identity.application;

import co.unicauca.piedrazul.identity.domain.entities.User;
import co.unicauca.piedrazul.identity.domain.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Al arrancar el servicio, republica todos los usuarios existentes a RabbitMQ
 * para que el appointment-service reconstruya su UserCache en Redis.
 * Sin esto, los usuarios insertados via SQL nunca llegan al cache y
 * el ExistenceAppointmentValidator lanza "Médico/Paciente no encontrado".
 */
@Component
public class StartupUserPublisher implements CommandLineRunner {

    private final UserRepository userRepository;
    private final UserEventPublisher userEventPublisher;

    public StartupUserPublisher(UserRepository userRepository,
                                UserEventPublisher userEventPublisher) {
        this.userRepository     = userRepository;
        this.userEventPublisher = userEventPublisher;
    }

    @Override
    public void run(String... args) {
        List<User> users = userRepository.findAll();
        for (User user : users) {
            userEventPublisher.publishUserRegistered(user);
        }
        System.out.printf("[StartupUserPublisher] %d usuarios publicados al cache de citas%n",
                users.size());
    }
}
