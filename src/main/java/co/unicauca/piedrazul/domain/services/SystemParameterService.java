/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package co.unicauca.piedrazul.domain.services;

import co.unicauca.piedrazul.domain.acces.ISystemParameterRepository;
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
