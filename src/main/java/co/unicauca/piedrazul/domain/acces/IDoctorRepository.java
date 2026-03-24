/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package co.unicauca.piedrazul.domain.acces;

import co.unicauca.piedrazul.domain.entities.Doctor;
import java.util.List;

/**
 *
 * @author santi
 */
public interface IDoctorRepository {
    
    // Inserta en users y doctors dentro de una sola transacción
    boolean save(Doctor doctor);
 
    // Busca por id, trae también roles y especialidades
    Doctor findById(int id);
 
    // Lista todos (activos e inactivos)
    List<Doctor> findAll();
 
    // Lista solo los activos (para el agendamiento)
    List<Doctor> findAllActive();
 
    // Actualiza datos del médico
    boolean update(Doctor doctor);
 
    // Cambia el estado a INACTIVO en la tabla users
    boolean desactivate(int id);
    
}
