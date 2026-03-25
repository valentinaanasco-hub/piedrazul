package co.unicauca.piedrazul.domain.services;

import co.unicauca.piedrazul.domain.access.ISpecialtyRepository;

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
