/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package co.unicauca.piedrazul.domain.acces;

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
    
    // Para registrar un nuevo paciente
    boolean save(Patient patient);

    // Para buscar un paciente por su id
    Patient findById(int id);

    // Para listar todos los pacientes
    List<Patient> findAll();

    // Para actualizar datos del paciente
    boolean update(Patient patient);

    // Para desactivar un paciente
    boolean desactivate(int id);
}
