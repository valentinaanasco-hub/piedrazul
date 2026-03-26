/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package co.unicauca.piedrazul.main;

import co.unicauca.piedrazul.domain.access.IAppointmentRepository;
import co.unicauca.piedrazul.domain.access.IDoctorRepository;
import co.unicauca.piedrazul.domain.access.IDoctorScheduleRepository;
import co.unicauca.piedrazul.domain.access.IPatientRepository;
import co.unicauca.piedrazul.domain.access.IRoleRepository;
import co.unicauca.piedrazul.domain.access.ISpecialtyRepository;
import co.unicauca.piedrazul.domain.access.ISystemParameterRepository;
import co.unicauca.piedrazul.domain.access.IUserRepository;
import co.unicauca.piedrazul.domain.entities.DoctorSchedule;
import co.unicauca.piedrazul.domain.services.ManualAppointmentService;
import co.unicauca.piedrazul.domain.services.AvailabilityService;
import co.unicauca.piedrazul.domain.services.DoctorService;
import co.unicauca.piedrazul.domain.services.PatientService;
import co.unicauca.piedrazul.domain.services.RoleService;
import co.unicauca.piedrazul.domain.services.SpecialtyService;
import co.unicauca.piedrazul.domain.services.SystemParameterService;
import co.unicauca.piedrazul.domain.services.UserService;
import co.unicauca.piedrazul.domain.services.interfaces.IManualAppointmentValidator;
import co.unicauca.piedrazul.domain.services.validators.DoctorScheduleValidator;
import co.unicauca.piedrazul.domain.services.validators.DoctorValidator;
import co.unicauca.piedrazul.domain.services.validators.ManualAppointmentValidator;
import co.unicauca.piedrazul.domain.services.validators.PatientValidator;
import co.unicauca.piedrazul.domain.services.validators.UserValidator;
import co.unicauca.piedrazul.infrastructure.repositories.PostgresAppointmentRepository;
import co.unicauca.piedrazul.infrastructure.repositories.PostgresDoctorRepository;
import co.unicauca.piedrazul.infrastructure.repositories.PostgresDoctorScheduleRepository;
import co.unicauca.piedrazul.infrastructure.repositories.PostgresPatientRepository;
import co.unicauca.piedrazul.infrastructure.repositories.PostgresSpecialtyRepository;
import co.unicauca.piedrazul.infrastructure.repositories.PostgresSystemParameterRepository;
import co.unicauca.piedrazul.infrastructure.repositories.PostgresUserRepository;

/**
 *
 * @author santi
 */
public class PostgresServiceFactory implements IServiceFactory {
    // Repositorios Postgres 
    private final IDoctorRepository doctorRepo = new PostgresDoctorRepository();
    private final IPatientRepository patientRepo = new PostgresPatientRepository();
    private final IAppointmentRepository appointmentRepo = new PostgresAppointmentRepository();
    private final IDoctorScheduleRepository scheduleRepo = new PostgresDoctorScheduleRepository();
    private final ISpecialtyRepository specialtyRepo = new PostgresSpecialtyRepository();
    private final IUserRepository userRepo = new PostgresUserRepository();
    private final ISystemParameterRepository paramRepo = new PostgresSystemParameterRepository();
    
    // Validadores
    private final UserValidator userValidator = new UserValidator();
    private final DoctorValidator doctorValidator = new DoctorValidator();
    private final PatientValidator patientValidator = new PatientValidator();
    private final DoctorScheduleValidator scheduleValidator = new DoctorScheduleValidator();
    private final IManualAppointmentValidator manualValidator = new ManualAppointmentValidator();
    
    @Override
    public UserService createUserService() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public DoctorService createDoctorService() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public DoctorSchedule createDoctorScheduleService() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public PatientService createPatientService() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public RoleService createRoleService() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public SpecialtyService createSpecialtyService() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public ManualAppointmentService createAppointmentService() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public SystemParameterService createSystemParameterService() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public AvailabilityService createAvailabilityService() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
}
