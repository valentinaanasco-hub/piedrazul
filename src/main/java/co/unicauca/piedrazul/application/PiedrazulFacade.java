package co.unicauca.piedrazul.application;

import co.unicauca.piedrazul.domain.services.interfaces.IServiceFactory;
import co.unicauca.piedrazul.main.DataBaseType;
import co.unicauca.piedrazul.main.ServiceFactoryProvider;

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
    
    /**
     * Obtiene la instancia única de la fachada.
     * @param dbType Tipo de base de datos a utilizar.
     * @return Instancia de PiedrazulFacade.
     */
    //Patron singleton
    public static PiedrazulFacade getInstance(DataBaseType dbType) { 
        if (instance == null) {
            // Obtenemos la fábrica concreta a través del Provider
            IServiceFactory factory = ServiceFactoryProvider.getFactory(dbType);
            instance = new PiedrazulFacade(factory);
        }
        return instance;
    }
    //Controladores

}
