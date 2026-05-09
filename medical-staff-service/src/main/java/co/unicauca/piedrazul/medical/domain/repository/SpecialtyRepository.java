package co.unicauca.piedrazul.medical.domain.repository;

import co.unicauca.piedrazul.medical.domain.entities.Specialty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio JPA para la entidad Specialty.
 *
 * @author Ginner Ortega
 */
@Repository
public interface SpecialtyRepository extends JpaRepository<Specialty, Integer> {}
