/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package co.unicauca.piedrazul.domain.acces;

import co.unicauca.piedrazul.domain.entities.Doctor;
import co.unicauca.piedrazul.domain.entities.Patient;
import java.util.List;

/**
 * @author Valentina Añasco 
 * @author Camila Dorado
 * @author Felipe Gutierrez
 * @author Ginner Ortega
 * @author Santiago Solarte 
 */

public interface IDoctorRepository {
    
    // Para registrar un nuevo médico en el sistema
    boolean save(Doctor doctor);

    // Para buscar un médico por su id de usuario
    Doctor findById(int id);

    // Para listar todos los médicos activos e inactivos
    List<Doctor> findAll();
    
    // Para listar todos los medico activos
    List<Doctor> findAllActive();

    // Para actualizar datos o estado del médico
    boolean update(Doctor doctor);
   
    // Para desactivar un paciente sin eliminar su historial
    boolean desactivate(int id);
}
