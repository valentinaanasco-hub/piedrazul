package co.unicauca.piedrazul.domain.services;

import co.unicauca.piedrazul.domain.access.IDoctorScheduleRepository;
import co.unicauca.piedrazul.domain.entities.DoctorSchedule;
import co.unicauca.piedrazul.domain.services.interfaces.IDoctorScheduleService;
import co.unicauca.piedrazul.domain.services.interfaces.IDoctorScheduleValidator;

import java.util.List;

/**
 * @author Valentina Añasco 
 * @author Camila Dorado
 * @author Felipe Gutierrez
 * @author Ginner Ortega
 * @author Santiago Solarte 
 */

public class DoctorScheduleService implements IDoctorScheduleService {
    
    private final IDoctorScheduleRepository scheduleRepository;
    private final IDoctorScheduleValidator validator;

    // Inyección de dependencias por constructor
    public DoctorScheduleService(IDoctorScheduleRepository scheduleRepository, 
                                 IDoctorScheduleValidator validator) {
        this.scheduleRepository = scheduleRepository;
        this.validator = validator;
    }

    
     // Registra un nuevo horario para un médico tras validar las reglas de negocio.
     
    @Override
    public boolean registerSchedule(DoctorSchedule schedule, int doctorId) {
        validator.validate(schedule);
        
        return scheduleRepository.save(schedule, doctorId);
    }

    // Retorna la lista de horarios asociados a un médico.
   
    @Override
    public List<DoctorSchedule> listSchedulesByDoctor(int doctorId) {
        List<DoctorSchedule> schedules = scheduleRepository.findByDoctorId(doctorId);
        return schedules;
    }

     // Modifica un horario existente validando que los nuevos datos sean consistentes.
    
    @Override
    public boolean modifySchedule(DoctorSchedule schedule) {
        // Validamos que el objeto enviado sea correcto (día, horas, intervalos)
        validator.validate(schedule);
        
        // Verificamos existencia antes de actualizar
        DoctorSchedule existing = scheduleRepository.findById(schedule.getScheduleId());
        validator.validateExists(existing);

        return scheduleRepository.update(schedule);
    }

    // Elimina horario del sistema
    @Override
    public boolean removeSchedule(int scheduleId) {
        // Verificamos que el horario exista antes de intentar borrarlo
        DoctorSchedule existing = scheduleRepository.findById(scheduleId);
        validator.validateExists(existing);
        
        return scheduleRepository.delete(scheduleId);
    }
   
    
}
