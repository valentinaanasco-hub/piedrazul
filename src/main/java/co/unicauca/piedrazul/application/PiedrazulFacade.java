package co.unicauca.piedrazul.application;

import co.unicauca.piedrazul.domain.entities.Patient;
import co.unicauca.piedrazul.domain.entities.Role;
import co.unicauca.piedrazul.domain.entities.User;
import co.unicauca.piedrazul.domain.entities.enums.RoleName;
import co.unicauca.piedrazul.domain.services.interfaces.IServiceFactory;
import co.unicauca.piedrazul.main.DataBaseType;
import co.unicauca.piedrazul.main.ServiceFactoryProvider;

/**
 * Punto de entrada único de la lógica de negocio para la capa de presentación.
 * Implementa el patrón Singleton y Facade.
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
     *
     * @param dbType Tipo de base de datos a utilizar.
     * @return Instancia de PiedrazulFacade.
     */
    public static PiedrazulFacade getInstance(DataBaseType dbType) {
        if (instance == null) {
            IServiceFactory factory = ServiceFactoryProvider.getFactory(dbType);
            instance = new PiedrazulFacade(factory);
        }
        return instance;
    }

    /**
     * Coordina el registro completo de un paciente.
     * PatientService.registerPatient() ya inserta en users y patients dentro de
     * una sola transacción, por lo que no se llama a UserService.registerUser()
     * para evitar la inserción duplicada en users.
     *
     * @param patient Objeto paciente con los datos del formulario.
     * @return true si el registro fue exitoso.
     */
    public boolean registerPatient(Patient patient) {
        // Se asigna el rol PACIENTE
        patient.addRole(new Role(RoleName.PACIENTE));

        return serviceFactory.createPatientService().registerPatient(patient);
    }
}
