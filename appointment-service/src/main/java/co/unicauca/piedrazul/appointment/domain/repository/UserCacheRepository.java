package co.unicauca.piedrazul.appointment.domain.repository;

import co.unicauca.piedrazul.appointment.domain.entities.UserCache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio JPA para la caché local de usuarios
 */
@Repository
public interface UserCacheRepository extends JpaRepository<UserCache, Integer> {
}
