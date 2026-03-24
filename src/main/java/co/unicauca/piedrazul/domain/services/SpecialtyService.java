/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package co.unicauca.piedrazul.domain.services;

import co.unicauca.piedrazul.domain.acces.ISpecialtyRepository;
import co.unicauca.piedrazul.domain.entities.Specialty;
import java.util.List;

/**
 * @author Valentina Añasco 
 * @author Camila Dorado
 * @author Felipe Gutierrez
 * @author Ginner Ortega
 * @author Santiago Solarte 
 */

public class SpecialtyService {
    private final ISpecialtyRepository specialtyRepository;

    public SpecialtyService(ISpecialtyRepository specialtyRepository) {
        this.specialtyRepository = specialtyRepository;
    }

}
