/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package co.unicauca.piedrazul.domain.services.interfaces;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 *
 * @author santi
 */
public interface IAvailabilityService {
    // Retorna los slots libres
    List<LocalTime> getAvailableSlots(int doctorId, LocalDate date);
    
    // Retorna el intervalo de tiempo del médico
    int getIntervalMinutesForDoctorOnDate(int doctorId, LocalDate date);
}
