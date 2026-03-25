package co.unicauca.piedrazul.domain.access;

import co.unicauca.piedrazul.domain.entities.Patient;
import java.util.List;

/**
 * @author Valentina Añasco
 * @author Camila Dorado
 * @author Felipe Gutierrez
 * @author Ginner Ortega
 * @author Santiago Solarte
 */
public interface IPatientRepository {

    // Para buscar un paciente por su id
    Patient findById(int id);

    // Para listar todos los pacientes
    List<Patient> findAll();

    // Para registrar un nuevo paciente
    boolean save(Patient patient);

    // Para actualizar datos del paciente
    boolean update(Patient patient);

    // Para desactivar un paciente
    boolean deactivate(int id);
}
