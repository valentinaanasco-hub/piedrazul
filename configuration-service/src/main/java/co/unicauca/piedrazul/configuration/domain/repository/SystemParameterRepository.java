package co.unicauca.piedrazul.configuration.domain.repository;

import co.unicauca.piedrazul.configuration.domain.entities.SystemParameter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para gestionar parámetros de configuración global del sistema.
 *
 * @author Santiago Solarte
 */
@Repository
public interface SystemParameterRepository extends JpaRepository<SystemParameter, String> {
}
