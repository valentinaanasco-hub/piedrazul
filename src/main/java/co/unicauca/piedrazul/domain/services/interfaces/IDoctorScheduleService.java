/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package co.unicauca.piedrazul.domain.services.interfaces;

import co.unicauca.piedrazul.domain.entities.DoctorSchedule;
import java.util.List;

/**
 *
 * @author santi
 */
public interface IDoctorScheduleService {
    // Registra una nueva jornada de atención para un médico
    boolean registerSchedule(DoctorSchedule schedule, int doctorId);

    // Recupera todos los horarios configurados para un médico específico
    List<DoctorSchedule> listSchedulesByDoctor(int doctorId);

    // Actualiza la información de un horario existente
    boolean modifySchedule(DoctorSchedule schedule);

    // Elimina permanentemente un horario del sistema
    boolean removeSchedule(int scheduleId);
}
