/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package co.unicauca.piedrazul.domain.acces;

import co.unicauca.piedrazul.domain.entities.SystemParameter;

/**
 *
 * @author santi
 */
public interface ISystemParameterRepository {
    SystemParameter findByKey(String key);
}
