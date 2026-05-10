package co.unicauca.piedrazul.patient.domain.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import co.unicauca.piedrazul.patient.domain.entities.Patient;

/**
 * Repositorio JPA para la entidad Patient.
 *
 * @author Santiago Solarte
 */
@Repository
public interface PatientRepository extends JpaRepository<Patient, Integer> {

    // Verifica si ya existe un paciente con ese id
    boolean existsById(int id);

    // Busca pacientes por email (campo pat_email)
    Optional<Patient> findByEmail(String email);

    // Busca pacientes por número de documento (id)
    @Query("SELECT p FROM Patient p WHERE p.id = ?1")
    Optional<Patient> findByDocumentId(int documentId);
}
