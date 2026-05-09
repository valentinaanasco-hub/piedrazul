package co.unicauca.piedrazul.medical.domain.repository;

import co.unicauca.piedrazul.medical.domain.entities.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para la entidad Doctor.
 *
 * @author Ginner Ortega
 */
@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Integer> {

    // Busca médicos por especialidad
    @Query("SELECT DISTINCT d FROM Doctor d JOIN d.specialties s WHERE s.id = ?1")
    List<Doctor> findBySpecialtyId(int specialtyId);

    // Verifica si existe un médico con ese id
    boolean existsById(int id);
}
