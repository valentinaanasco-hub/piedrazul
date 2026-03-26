package co.unicauca.piedrazul.domain.services;

import co.unicauca.piedrazul.domain.access.ISystemParameterRepository;
import co.unicauca.piedrazul.domain.entities.SystemParameter;
import co.unicauca.piedrazul.domain.services.interfaces.ISystemParameterService;
import co.unicauca.piedrazul.domain.services.interfaces.ISystemParameterValidator;

/**
 *
 * @author santi
 */
public class SystemParameterService implements ISystemParameterService {
   
    private final ISystemParameterRepository systemParameterRepository;
    private final ISystemParameterValidator validator; // Inyectamos la interfaz
    
    public SystemParameterService(ISystemParameterRepository systemParameterRepository, 
                                  ISystemParameterValidator validator) {
        this.systemParameterRepository = systemParameterRepository;
        this.validator = validator;
    }
    
    @Override
    public SystemParameter findParameter(String key) {
        // Validamos que la llave recibida sea un texto válido
        validator.validateKey(key);
        
        // Buscamos en el repositorio
        SystemParameter parameter = systemParameterRepository.findByKey(key);
        
        // Validamos que realmente se haya encontrado algo
        validator.validateExists(parameter, key);
        
        return parameter;
    }
}
