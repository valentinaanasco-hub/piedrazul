/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package co.unicauca.piedrazul.domain.services.validators;

import co.unicauca.piedrazul.domain.entities.DoctorSchedule;
import co.unicauca.piedrazul.domain.services.interfaces.IDoctorScheduleValidator;
import java.time.LocalTime;

/**
 *
 * @author santi
 */
public class DoctorScheduleValidator implements IDoctorScheduleValidator {
    @Override
    public void validate(DoctorSchedule schedule) {
        validateDayOfWeek(schedule.getDayOfWeek());
        validateTimeRange(schedule.getStartTime(), schedule.getEndTime());
        validateInterval(schedule.getIntervalMinutes());
    }

    
    private void validateDayOfWeek(int day) {
        if (day < 1 || day > 7) 
            throw new IllegalArgumentException("Día inválido: debe ser entre 1 y 7");
    }

   
    private void validateTimeRange(LocalTime start, LocalTime end) {
        if (start == null || end == null || !start.isBefore(end))
            throw new IllegalArgumentException("Rango de horas inválido");
    }

    
    private void validateInterval(int interval) {
        if (interval <= 0)
            throw new IllegalArgumentException("El intervalo debe ser mayor a cero");
    }
    
    @Override
    public void validateExists(DoctorSchedule doctorSchedule) {
        if (doctorSchedule == null) {
            throw new IllegalArgumentException("Médico no encontrado");
        }
    }

}
