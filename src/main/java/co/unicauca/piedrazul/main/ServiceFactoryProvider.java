package co.unicauca.piedrazul.main;

import co.unicauca.piedrazul.infrastructure.factories.PostgresServiceFactory;
import co.unicauca.piedrazul.domain.services.interfaces.IServiceFactory;

/**
 *
 * @author santi
 */
public class ServiceFactoryProvider {
    
    // Retorna la fábrica de servicios correspondiente al tipo de base de datos.
    
    public static IServiceFactory getFactory(DataBaseType dbType) {
        switch (dbType) {
            case POSTGRESQL -> {
                return new PostgresServiceFactory();
            }
            case SQLITE -> // Aquí retornarías new SqliteServiceFactory() cuando se implemente
                throw new UnsupportedOperationException("SQLite no implementado aún.");
            case MYSQL -> throw new UnsupportedOperationException("MySQL no implementado aún.");
            default -> {
                return new PostgresServiceFactory();
            }
        }
    }
    
}
