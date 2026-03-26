/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package co.unicauca.piedrazul.application;

import co.unicauca.piedrazul.domain.services.interfaces.IServiceFactory;

/**
 *
 * @author santi
 */
public class PiedrazulFacade {
    private static PiedrazulFacade instance;
    private final IServiceFactory serviceFactory;

    private PiedrazulFacade(IServiceFactory factory) {
        this.serviceFactory = factory;
    }
    //Controladores

}
