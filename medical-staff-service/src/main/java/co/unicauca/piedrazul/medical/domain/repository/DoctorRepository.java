package co.unicauca.piedrazul.medical.domain.repository;

import co.unicauca.piedrazul.medical.domain.entities.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio JPA para la entidad Doctor.
 *
 * @author Ginner Ortega
 */
@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Integer> {

    /**
     * Lista todos los médicos con su nombre completo desde la tabla doctors.
     */
    @Query(value = """
        SELECT d.doct_user_id, d.doct_professional_id,
               d.doct_first_name, NULL AS doct_middle_name,
               d.doct_first_surname, NULL AS doct_last_name
        FROM doctors d
        """, nativeQuery = true)
    List<Object[]> findAllWithNames();

    /**
     * Busca médicos por especialidad con nombre completo.
     */
    @Query(value = """
        SELECT d.doct_user_id, d.doct_professional_id,
               d.doct_first_name, NULL AS doct_middle_name,
               d.doct_first_surname, NULL AS doct_last_name
        FROM doctors d
        JOIN doctor_specialties ds ON ds.ds_doct_id = d.doct_user_id
        WHERE ds.ds_spec_id = :specialtyId
        """, nativeQuery = true)
    List<Object[]> findBySpecialtyIdWithNames(int specialtyId);

    boolean existsById(int id);
}