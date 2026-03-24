/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package co.unicauca.piedrazul.domain.acces;

import co.unicauca.piedrazul.domain.entities.Doctor;
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
    
  // Registra una especialidad nueva en el catálogo
    boolean save(Specialty specialty);
 
    // Busca por id para validaciones
    Specialty findById(int id);
 
    // Busca por nombre (útil para evitar duplicados)
    Specialty findByName(String name);
 
    // Lista el catálogo completo de especialidades
    List<Specialty> findAll();
 
    // Asocia una especialidad existente a un médico en doctor_specialties
    boolean assignSpecialtyToDoctor(int doctorId, int specialtyId);
 
    // Devuelve las especialidades que tiene un médico concreto
    List<Specialty> findByDoctorId(int doctorId);

}

