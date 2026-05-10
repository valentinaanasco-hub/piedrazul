package co.unicauca.piedrazul.appointment.domain.repository;

import co.unicauca.piedrazul.appointment.domain.entities.UserCache;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio Redis para la caché local de usuarios
 * Usa CrudRepository de Spring Data Redis en vez de JpaRepository
 */
@Repository
public interface UserCacheRepository extends CrudRepository<UserCache, Integer> {
}
