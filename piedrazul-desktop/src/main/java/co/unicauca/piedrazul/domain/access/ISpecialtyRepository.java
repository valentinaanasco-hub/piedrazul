package co.unicauca.piedrazul.domain.access;

import co.unicauca.piedrazul.domain.entities.Specialty;
import java.util.List;

/**
 * @author Valentina Añasco
 * @author Camila Dorado
 * @author Felipe Gutierrez
 * @author Ginner Ortega
 * @author Santiago Solarte
 */
public interface ISpecialtyRepository {


    // Busca por id para validaciones
    Specialty findById(int id);

    // Busca por nombre (útil para evitar duplicados)
    Specialty findByName(String name);

    // Lista el catálogo completo de especialidades
    List<Specialty> findAll();

    // Asocia una especialidad existente a un médico en doctor_specialties
    boolean assignSpecialtyToDoctor(int doctorId, int specialtyId);

    // Devuelve las especialidades que tiene un médico concreto
    List<Specialty> findSpecialtiesByDoctorId(int doctorId);

}
