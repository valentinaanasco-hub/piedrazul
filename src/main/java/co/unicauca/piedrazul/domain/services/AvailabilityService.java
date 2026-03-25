package co.unicauca.piedrazul.domain.services;

import co.unicauca.piedrazul.domain.acces.IDoctorScheduleRepository;
import co.unicauca.piedrazul.domain.entities.Appointment;
import co.unicauca.piedrazul.domain.entities.DoctorSchedule;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import co.unicauca.piedrazul.domain.acces.IAppointmentRepository;

/**
 *
 * @author santi
 */
public class AvailabilityService {
 
    private final IDoctorScheduleRepository scheduleRepository;
    private final IAppointmentRepository appointmentRepository;
 
    // Inyección por constructor (DIP): depende de interfaces, no implementaciones
    public AvailabilityService(IDoctorScheduleRepository scheduleRepository,
                               IAppointmentRepository appointmentRepository) {
        this.scheduleRepository    = scheduleRepository;
        this.appointmentRepository = appointmentRepository;
    }
    
    // Calcula los slots de hora LIBRES para un médico en una fecha dada.
    public List<LocalTime> getAvailableSlots(int doctorId, LocalDate date) {
        List<LocalTime> availableSlots = new ArrayList<>();
 
        // 1=Lunes ... 7=Domingo
        int dayOfWeek = date.getDayOfWeek().getValue();
 
        // Paso 1: obtiene los horarios configurados del médico
        List<DoctorSchedule> schedules = scheduleRepository.findByDoctorId(doctorId);
 
        // Paso 2: trae TODAS las citas del médico en esa fecha
        List<Appointment> appointmentsOnDate =
            appointmentRepository.findByDoctorAndDate(doctorId, date.toString());
 
        // Paso 3: Set con las horas de inicio ya ocupadas
        Set<LocalTime> occupiedStartTimes = buildOccupiedSet(appointmentsOnDate);
 
        // Paso 4: genera los slots del horario y filtra los ocupados
        for (DoctorSchedule schedule : schedules) {
            // Solo procesa el horario del día de la semana solicitado
            if (schedule.getDayOfWeek() != dayOfWeek) continue;
 
            LocalTime cursor = schedule.getStartTime();
            int intervalMinutes = schedule.getIntervalMinutes();
 
            while (cursor.isBefore(schedule.getEndTime())) {
                // Verifica en memoria si este slot ya está ocupado (O(1))
                if (!occupiedStartTimes.contains(cursor)) {
                    availableSlots.add(cursor); // slot libre
                }
                cursor = cursor.plusMinutes(intervalMinutes); // avanza al siguiente
            }
        }
 
        return availableSlots;
    }
 
   
    // Retorna el intervalo de atención (minutos) del médico para el día
    // de la semana correspondiente a la fecha dada.
    public int getIntervalMinutesForDoctorOnDate(int doctorId, LocalDate date) {
        int dayOfWeek = date.getDayOfWeek().getValue();
        List<DoctorSchedule> schedules = scheduleRepository.findByDoctorId(doctorId);
 
        for (DoctorSchedule schedule : schedules) {
            if (schedule.getDayOfWeek() == dayOfWeek) {
                return schedule.getIntervalMinutes();
            }
        }
 
        return 30; // 30 minutos si no hay horario configurado
    }
 
   
    // Construye un Set de horas de inicio ocupadas a partir de las citas existentes.
     
    private Set<LocalTime> buildOccupiedSet(List<Appointment> appointments) {
        Set<LocalTime> occupied = new HashSet<>();
 
        if (appointments == null) return occupied; // repositorio retornó null
 
        for (Appointment appt : appointments) {
            // Las citas canceladas liberan el slot
            if ("CANCELADA".equals(appt.getStatus())) continue;
            occupied.add(appt.getStartTime());
        }
 
        return occupied;
    }
}
