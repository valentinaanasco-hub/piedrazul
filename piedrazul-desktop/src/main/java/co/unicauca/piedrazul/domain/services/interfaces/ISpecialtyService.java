/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package co.unicauca.piedrazul.domain.services.interfaces;

import co.unicauca.piedrazul.domain.entities.Specialty;
import java.util.List;

/**
 *
 * @author santi
 */
public interface ISpecialtyService {
    // Busca una especialidad por su nombre exacto
    Specialty findByName(String name);

    // Busca una especialidad por su ID
    Specialty findSpecialty(int id);

    // Retorna todas las especialidades registradas en el sistema
    List<Specialty> listSpecialties();

    // Asocia una especialidad clínica a un médico
    boolean assignSpecialtyToDoctor(int doctorId, int specialtyId);

    // Lista las especialidades que domina un médico en particular
    List<Specialty> findByDoctorId(int doctorId);
}
