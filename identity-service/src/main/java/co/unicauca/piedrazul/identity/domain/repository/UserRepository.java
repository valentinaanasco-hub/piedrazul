package co.unicauca.piedrazul.identity.domain.repository;

import co.unicauca.piedrazul.identity.domain.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * Repositorio JPA para la entidad User.
 *
 * @author Santiago Solarte
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Busca un usuario por su username (correo o nombre de usuario)
    Optional<User> findByUsername(String username);

    // Verifica si ya existe un usuario con ese username
    boolean existsByUsername(String username);
}
