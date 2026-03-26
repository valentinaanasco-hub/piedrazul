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
 *
 * @author santi
 */
public class PostgresServiceFactory implements IServiceFactory {
// --- Repositorios (Infraestructura) ---

    private final IDoctorRepository doctorRepo = new PostgresDoctorRepository();
    private final IPatientRepository patientRepo = new PostgresPatientRepository();
    private final IAppointmentRepository appointmentRepo = new PostgresAppointmentRepository();
    private final IDoctorScheduleRepository scheduleRepo = new PostgresDoctorScheduleRepository();
    private final ISpecialtyRepository specialtyRepo = new PostgresSpecialtyRepository();
    private final IUserRepository userRepo = new PostgresUserRepository();
    private final ISystemParameterRepository paramRepo = new PostgresSystemParameterRepository();
    private final IRoleRepository roleRepo = new PostgresRoleRepository();

    // --- Validadores (Lógica de Negocio) ---
    private final UserValidator userValidator = new UserValidator();
    private final DoctorValidator doctorValidator = new DoctorValidator();
    private final PatientValidator patientValidator = new PatientValidator();
    private final ManualAppointmentValidator manualValidator = new ManualAppointmentValidator();
    private final DoctorScheduleValidator scheduleValidator = new DoctorScheduleValidator();
    private final RoleValidator roleValidator = new RoleValidator();
    private final SpecialtyValidator specialtyValidator = new SpecialtyValidator();
    private final SystemParameterValidator systemParameterValidator = new SystemParameterValidator();

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
        return new ManualAppointmentService(
                appointmentRepo,
                doctorRepo,
                patientRepo,
                manualValidator
        );
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
