package co.unicauca.piedrazul.application;

import co.unicauca.piedrazul.domain.entities.Patient;
import co.unicauca.piedrazul.domain.entities.Role;
import co.unicauca.piedrazul.domain.entities.User;
import co.unicauca.piedrazul.domain.entities.enums.RoleName;
import co.unicauca.piedrazul.domain.services.interfaces.IAppointmentService;
import co.unicauca.piedrazul.domain.services.interfaces.IServiceFactory;
import co.unicauca.piedrazul.main.DataBaseType;
import co.unicauca.piedrazul.main.ServiceFactoryProvider;
import co.unicauca.piedrazul.presentation.controllers.*;

import java.util.List;

/**
 * Fachada del sistema Piedrazul. Centraliza el acceso a todos los controladores
 * del sistema. Implementa el patrón Singleton y Facade.
 *
 * @author santi
 */
public class PiedrazulFacade {

    private static PiedrazulFacade instance;
    private final IServiceFactory serviceFactory;

    // Controladores (Lazy Initialization)
    private UserController userController;
    private DoctorController doctorController;
    private PatientController patientController;
    private ManualAppointmentController appointmentController;
    private AvailabilityController availabilityController;
    private DoctorScheduleController scheduleController;
    private SpecialtyController specialtyController;
    private RoleController roleController;
    private SystemParameterController parameterController;
    private RegisterAppointmentController registerAppointmentController;

    private PiedrazulFacade(IServiceFactory factory) {
        this.serviceFactory = factory;
    }

    // Obtiene la instancia única de la fachada (Singleton)
    public static PiedrazulFacade getInstance(DataBaseType dbType) {
        if (instance == null) {
            IServiceFactory factory = ServiceFactoryProvider.getFactory(dbType);
            instance = new PiedrazulFacade(factory);
        }
        return instance;
    }

    /**
     * Autentica al usuario y carga sus roles desde la BD. UserService.login()
     * solo trae datos de la tabla users — los roles viven en users_roles y se
     * cargan por separado.
     *
     * @param username Correo o nombre de usuario.
     * @param password Contraseña en texto plano.
     * @return Usuario autenticado con roles cargados, o null si falla.
     */
    public User login(String username, String password) {
        User user = serviceFactory.createUserService().login(username, password);
        if (user != null) {
            List<Role> roles = serviceFactory.createRoleService().listRolesByUser(user.getId());
            user.setRoles(roles);
        }
        return user;
    }

    // Coordina el registro completo de un paciente desde la vista de registro.
    // PatientService maneja users + patients en una sola transacción.
    public boolean registerPatient(Patient patient) {
        patient.addRole(new Role(RoleName.PACIENTE));
        return serviceFactory.createPatientService().registerPatient(patient);
    }

    // Métodos para obtener los controladores
    public UserController getUserController() {
        if (userController == null) {
            userController = new UserController(serviceFactory.createUserService());
        }
        return userController;
    }

    public DoctorController getDoctorController() {
        if (doctorController == null) {
            doctorController = new DoctorController(serviceFactory.createDoctorService());
        }
        return doctorController;
    }

    public PatientController getPatientController() {
        if (patientController == null) {
            patientController = new PatientController(serviceFactory.createPatientService());
        }
        return patientController;
    }

    public ManualAppointmentController getManualAppointmentController() {
        if (appointmentController == null) {
            appointmentController = new ManualAppointmentController((IAppointmentService) serviceFactory.createManualAppointmentService());
        }
        return appointmentController;
    }

    public AvailabilityController getAvailabilityController() {
        if (availabilityController == null) {
            availabilityController = new AvailabilityController(serviceFactory.createAvailabilityService());
        }
        return availabilityController;
    }

    public DoctorScheduleController getScheduleController() {
        if (scheduleController == null) {
            scheduleController = new DoctorScheduleController(serviceFactory.createDoctorScheduleService());
        }
        return scheduleController;
    }

    public SpecialtyController getSpecialtyController() {
        if (specialtyController == null) {
            specialtyController = new SpecialtyController(serviceFactory.createSpecialtyService());
        }
        return specialtyController;
    }

    public RoleController getRoleController() {
        if (roleController == null) {
            roleController = new RoleController(serviceFactory.createRoleService());
        }
        return roleController;
    }

    public SystemParameterController getParameterController() {
        if (parameterController == null) {
            parameterController = new SystemParameterController(serviceFactory.createSystemParameterService());
        }
        return parameterController;
    }

    public RegisterAppointmentController getRegisterAppointmentController() {
        if (registerAppointmentController == null) {
            registerAppointmentController = new RegisterAppointmentController(
                    serviceFactory.createManualAppointmentService(),
                    serviceFactory.createDoctorService(),
                    serviceFactory.createAvailabilityService(),
                    serviceFactory.createPatientService(),
                    serviceFactory.createSystemParameterService()
            );
        }
        return registerAppointmentController;
    }
}
