package co.unicauca.piedrazul.identity.domain.repository;

import co.unicauca.piedrazul.identity.domain.entities.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * Repositorio JPA para la entidad Role.
 *
 * @author Santiago Solarte
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {

    // Busca un rol por su nombre
    Optional<Role> findByRoleName(String roleName);
}
