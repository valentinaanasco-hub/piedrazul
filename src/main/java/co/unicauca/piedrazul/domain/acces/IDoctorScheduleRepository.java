/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package co.unicauca.piedrazul.domain.acces;

import co.unicauca.piedrazul.domain.entities.DoctorSchedule;
import java.util.List;

/**
 * @author Valentina Añasco 
 * @author Camila Dorado
 * @author Felipe Gutierrez
 * @author Ginner Ortega
 * @author Santiago Solarte 
 */

public interface IDoctorScheduleRepository {
   
    // Para registrar un horario disponible de un médico
    boolean save(DoctorSchedule schedule, int doctorId);

    // Para consultar todos los horarios de un médico específico
    List<DoctorSchedule> findByDoctorId(int doctorId);

    // Para modificar un horario existente
    boolean update(DoctorSchedule schedule);

    // Para eliminar un horario por su id
    boolean delete(int scheduleId);
}
