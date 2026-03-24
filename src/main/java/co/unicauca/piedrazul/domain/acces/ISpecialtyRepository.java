/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package co.unicauca.piedrazul.domain.acces;

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
    
    // Para registrar una nueva especialidad
    boolean save(Specialty specialty);

    // Para buscar una especialidad por su id
    Specialty findById(int id);

    // Para listar todas las especialidades disponibles
    List<Specialty> findAll();
}

