/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package co.unicauca.piedrazul.domain.services.interfaces;

import co.unicauca.piedrazul.domain.entities.SystemParameter;

/**
 *
 * @author santi
 */
public interface ISystemParameterService {
    // Recupera un parámetro de configuración global mediante su clave
    SystemParameter findParameter(String key);
}
