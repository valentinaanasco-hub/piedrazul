/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package co.unicauca.piedrazul.domain.services.interfaces;

import co.unicauca.piedrazul.domain.entities.DoctorSchedule;

/**
 *
 * @author santi
 */
public interface IDoctorScheduleValidator {
    
    void validate(DoctorSchedule schedule);
    void validateExists(DoctorSchedule doctorSchedule);
}
