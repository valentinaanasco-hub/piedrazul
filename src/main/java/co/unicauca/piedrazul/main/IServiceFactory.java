/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package co.unicauca.piedrazul.main;

import co.unicauca.piedrazul.domain.entities.DoctorSchedule;
import co.unicauca.piedrazul.domain.services.ManualAppointmentService;
import co.unicauca.piedrazul.domain.services.AvailabilityService;
import co.unicauca.piedrazul.domain.services.DoctorService;
import co.unicauca.piedrazul.domain.services.PatientService;
import co.unicauca.piedrazul.domain.services.RoleService;
import co.unicauca.piedrazul.domain.services.SpecialtyService;
import co.unicauca.piedrazul.domain.services.SystemParameterService;
import co.unicauca.piedrazul.domain.services.UserService;

/**
 *
 * @author santi
 */
public interface IServiceFactory {
    UserService createUserService();
    DoctorService createDoctorService();
    DoctorSchedule createDoctorScheduleService();
    PatientService createPatientService();
    RoleService createRoleService();
    SpecialtyService createSpecialtyService();
    ManualAppointmentService createAppointmentService();
    SystemParameterService createSystemParameterService();
    AvailabilityService createAvailabilityService();
}
