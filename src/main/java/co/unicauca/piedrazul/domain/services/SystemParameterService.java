package co.unicauca.piedrazul.domain.services;

import co.unicauca.piedrazul.domain.access.ISystemParameterRepository;
import co.unicauca.piedrazul.domain.entities.SystemParameter;

/**
 *
 * @author santi
 */
public class SystemParameterService {
   
    private final ISystemParameterRepository systemParameterRepository;
    
    public SystemParameterService(ISystemParameterRepository systemParameterRepository){
        this.systemParameterRepository = systemParameterRepository;
    }
    
    public SystemParameter findParameter(String key){
        return systemParameterRepository.findByKey(key);
    }
    
}
