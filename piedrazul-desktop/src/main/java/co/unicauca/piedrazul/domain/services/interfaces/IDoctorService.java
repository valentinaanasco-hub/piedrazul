
package co.unicauca.piedrazul.domain.services.interfaces;


import co.unicauca.piedrazul.domain.entities.Doctor;
import java.util.List;

/**
 *
 * @author santi
 */
public interface IDoctorService {
    
    // Almacena un nuevo médico validando sus datos base
    boolean registerDoctor(Doctor doctor);

    // Busca un médico por su identificador único
    Doctor findDoctor(int id);

    // Retorna la lista de médicos con estado ACTIVO
    List<Doctor> listActiveDoctors();

    // Actualiza la información profesional o personal del médico
    boolean modifyDoctor(Doctor doctor);

    // Cambia el estado del médico a INACTIVO
    boolean deactivateDoctor(int id);
    

}
