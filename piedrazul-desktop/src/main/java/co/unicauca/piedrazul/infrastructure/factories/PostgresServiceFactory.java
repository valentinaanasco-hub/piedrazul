package co.unicauca.piedrazul.infrastructure.factories;

import co.unicauca.piedrazul.domain.services.interfaces.IServiceFactory;
import co.unicauca.piedrazul.domain.access.IAppointmentRepository;
import co.unicauca.piedrazul.domain.access.IDoctorRepository;
import co.unicauca.piedrazul.domain.access.IDoctorScheduleRepository;
import co.unicauca.piedrazul.domain.access.IPatientRepository;
import co.unicauca.piedrazul.domain.access.IRoleRepository;
import co.unicauca.piedrazul.domain.access.ISpecialtyRepository;
import co.unicauca.piedrazul.domain.access.ISystemParameterRepository;
import co.unicauca.piedrazul.domain.access.IUserRepository;
import co.unicauca.piedrazul.domain.services.ManualAppointmentService;
import co.unicauca.piedrazul.domain.services.AvailabilityService;
import co.unicauca.piedrazul.domain.services.DoctorScheduleService;
import co.unicauca.piedrazul.domain.services.DoctorService;
import co.unicauca.piedrazul.domain.services.PatientService;
import co.unicauca.piedrazul.domain.services.RoleService;
import co.unicauca.piedrazul.domain.services.SpecialtyService;
import co.unicauca.piedrazul.domain.services.SystemParameterService;
import co.unicauca.piedrazul.domain.services.UserService;
import co.unicauca.piedrazul.domain.services.validators.DoctorScheduleValidator;
import co.unicauca.piedrazul.domain.services.validators.DoctorValidator;
import co.unicauca.piedrazul.domain.services.validators.ManualAppointmentValidator;
import co.unicauca.piedrazul.domain.services.validators.PatientValidator;
import co.unicauca.piedrazul.domain.services.validators.RoleValidator;
import co.unicauca.piedrazul.domain.services.validators.SpecialtyValidator;
import co.unicauca.piedrazul.domain.services.validators.SystemParameterValidator;
import co.unicauca.piedrazul.domain.services.validators.UserValidator;
import co.unicauca.piedrazul.infrastructure.repositories.PostgresAppointmentRepository;
import co.unicauca.piedrazul.infrastructure.repositories.PostgresDoctorRepository;
import co.unicauca.piedrazul.infrastructure.repositories.PostgresDoctorScheduleRepository;
import co.unicauca.piedrazul.infrastructure.repositories.PostgresPatientRepository;
import co.unicauca.piedrazul.infrastructure.repositories.PostgresRoleRepository;
import co.unicauca.piedrazul.infrastructure.repositories.PostgresSpecialtyRepository;
import co.unicauca.piedrazul.infrastructure.repositories.PostgresSystemParameterRepository;
import co.unicauca.piedrazul.infrastructure.repositories.PostgresUserRepository;

/**
 * @author Valentina Añasco
 * @author Camila Dorado
 * @author Felipe Gutierrez
 * @author Ginner Ortega
 * @author Santiago Solarte
 */
public class PostgresServiceFactory implements IServiceFactory {

    // Instancia única de la fábrica
    private static PostgresServiceFactory instance;

    // Repositorios: una sola instancia compartida por todos los servicios
    private final IDoctorRepository doctorRepo;
    private final IPatientRepository patientRepo;
    private final IAppointmentRepository appointmentRepo;
    private final IDoctorScheduleRepository scheduleRepo;
    private final ISpecialtyRepository specialtyRepo;
    private final IUserRepository userRepo;
    private final ISystemParameterRepository paramRepo;
    private final IRoleRepository roleRepo;

    // Validadores: sin estado, se pueden compartir igual que los repositorios
    private final UserValidator userValidator;
    private final DoctorValidator doctorValidator;
    private final PatientValidator patientValidator;
    private final ManualAppointmentValidator manualValidator;
    private final DoctorScheduleValidator scheduleValidator;
    private final RoleValidator roleValidator;
    private final SpecialtyValidator specialtyValidator;
    private final SystemParameterValidator systemParameterValidator;

    // Constructor privado: solo se ejecuta una vez
    public PostgresServiceFactory() {
        //Repositorios
        doctorRepo = new PostgresDoctorRepository();
        patientRepo = new PostgresPatientRepository();
        appointmentRepo = new PostgresAppointmentRepository();
        scheduleRepo = new PostgresDoctorScheduleRepository();
        specialtyRepo = new PostgresSpecialtyRepository();
        userRepo = new PostgresUserRepository();
        paramRepo = new PostgresSystemParameterRepository();
        roleRepo = new PostgresRoleRepository();
        //Validadores
        userValidator = new UserValidator();
        doctorValidator = new DoctorValidator();
        patientValidator = new PatientValidator();
        manualValidator = new ManualAppointmentValidator();
        scheduleValidator = new DoctorScheduleValidator();
        roleValidator = new RoleValidator();
        specialtyValidator = new SpecialtyValidator();
        systemParameterValidator = new SystemParameterValidator();
    }

    // Punto de acceso único a la fábrica
    public static PostgresServiceFactory getInstance() {
        if (instance == null) {
            instance = new PostgresServiceFactory();
        }
        return instance;
    }

    @Override
    public UserService createUserService() {
        return new UserService(userRepo, userValidator);
    }

    @Override
    public DoctorService createDoctorService() {
        return new DoctorService(doctorRepo, doctorValidator);
    }

    @Override
    public DoctorScheduleService createDoctorScheduleService() {
        return new DoctorScheduleService(scheduleRepo, scheduleValidator);
    }

    @Override
    public PatientService createPatientService() {
        return new PatientService(patientRepo, patientValidator);
    }

    @Override
    public RoleService createRoleService() {
        return new RoleService(roleRepo, roleValidator);
    }

    @Override
    public SpecialtyService createSpecialtyService() {
        return new SpecialtyService(specialtyRepo, specialtyValidator);
    }

    @Override
    public ManualAppointmentService createManualAppointmentService() {
        return new ManualAppointmentService(appointmentRepo, doctorRepo, patientRepo, manualValidator);
    }

    @Override
    public SystemParameterService createSystemParameterService() {
        return new SystemParameterService(paramRepo, systemParameterValidator);
    }

    @Override
    public AvailabilityService createAvailabilityService() {
        return new AvailabilityService(scheduleRepo, appointmentRepo);
    }
}