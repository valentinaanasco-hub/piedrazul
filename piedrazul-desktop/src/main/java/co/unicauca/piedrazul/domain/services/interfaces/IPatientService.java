/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package co.unicauca.piedrazul.domain.services.interfaces;

import co.unicauca.piedrazul.domain.entities.Patient;
import java.util.List;

/**
 *
 * @author santi
 */
public interface IPatientService {
    
    // Registra un nuevo paciente en el sistema
    boolean registerPatient(Patient patient);

    // Busca un paciente por su ID (documento)
    Patient findPatient(int id);

    // Retorna la lista de todos los pacientes
    List<Patient> listPatients();

    // Actualiza los datos de un paciente existente
    boolean modifyPatient(Patient patient);

    // Cambia el estado del paciente a inactivo
    boolean deactivatePatient(int id);
}
