/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package co.unicauca.piedrazul.domain.services.interfaces;

import co.unicauca.piedrazul.domain.entities.DoctorSchedule;
import co.unicauca.piedrazul.domain.services.ManualAppointmentService;
import co.unicauca.piedrazul.domain.services.AvailabilityService;
import co.unicauca.piedrazul.domain.services.DoctorScheduleService;
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
    // Gestión de accesos y usuarios
    UserService createUserService();
    RoleService createRoleService();

    // Personal médico y especialidades
    DoctorService createDoctorService();
    SpecialtyService createSpecialtyService();
    DoctorScheduleService createDoctorScheduleService();

    // Gestión de pacientes
    PatientService createPatientService();

    // Agendamiento y disponibilidad
    ManualAppointmentService createAppointmentService();
    AvailabilityService createAvailabilityService();

    // Configuración global del sistema
    SystemParameterService createSystemParameterService();
}
