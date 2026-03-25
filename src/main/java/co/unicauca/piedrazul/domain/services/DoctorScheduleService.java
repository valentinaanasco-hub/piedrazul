package co.unicauca.piedrazul.domain.services;

import co.unicauca.piedrazul.domain.acces.IDoctorScheduleRepository;
import co.unicauca.piedrazul.domain.entities.DoctorSchedule;

import java.util.List;

/**
 * @author Valentina Añasco 
 * @author Camila Dorado
 * @author Felipe Gutierrez
 * @author Ginner Ortega
 * @author Santiago Solarte 
 */

public class DoctorScheduleService {
      private final IDoctorScheduleRepository scheduleRepository;

    public DoctorScheduleService(IDoctorScheduleRepository scheduleRepository) {
        this.scheduleRepository = scheduleRepository;
    }

    public boolean registerSchedule(DoctorSchedule schedule, int doctorId) {
        // Valida que el día de la semana sea válido (1=Lunes, 7=Domingo)
        if (schedule.getDayOfWeek() < 1 || schedule.getDayOfWeek() > 7)
            throw new IllegalArgumentException("El día debe estar entre 1 y 7");

        // Valida que la hora de inicio sea antes que la de fin
        if (!schedule.getStartTime().isBefore(schedule.getEndTime()))
            throw new IllegalArgumentException("La hora de inicio debe ser antes que la de fin");

        // Valida que el intervalo sea positivo
        if (schedule.getIntervalMinutes() <= 0)
            throw new IllegalArgumentException("El intervalo debe ser mayor a 0");

        return scheduleRepository.save(schedule, doctorId);
    }

    public List<DoctorSchedule> listSchedulesByDoctor(int doctorId) {
        // Para mostrar la disponibilidad de un médico
        return scheduleRepository.findByDoctorId(doctorId);
    }

    public boolean modifySchedule(DoctorSchedule schedule) {
        // Valida que la hora de inicio sea antes que la de fin
        if (!schedule.getStartTime().isBefore(schedule.getEndTime()))
            throw new IllegalArgumentException("La hora de inicio debe ser antes que la de fin");
        return scheduleRepository.update(schedule);
    }

    public boolean removeSchedule(int scheduleId) {
        return scheduleRepository.delete(scheduleId);
    }
   
    
}
