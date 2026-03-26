package co.unicauca.piedrazul.domain.services;

import co.unicauca.piedrazul.domain.access.IDoctorScheduleRepository;
import co.unicauca.piedrazul.domain.access.IAppointmentRepository;
import co.unicauca.piedrazul.domain.entities.Appointment;
import co.unicauca.piedrazul.domain.entities.DoctorSchedule;
import co.unicauca.piedrazul.domain.entities.enums.AppointmentStatus;
import co.unicauca.piedrazul.domain.services.interfaces.IAvailabilityService;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author santi
 */
public class AvailabilityService implements IAvailabilityService {

    private final IDoctorScheduleRepository scheduleRepository;
    private final IAppointmentRepository appointmentRepository;

    // Inyección por constructor (DIP)
    public AvailabilityService(IDoctorScheduleRepository scheduleRepository,
                               IAppointmentRepository appointmentRepository) {
        this.scheduleRepository = scheduleRepository;
        this.appointmentRepository = appointmentRepository;
    }

    @Override
    public List<LocalTime> getAvailableSlots(int doctorId, LocalDate date) {
        List<LocalTime> availableSlots = new ArrayList<>();

        // 1=Lunes ... 7=Domingo
        int dayOfWeek = date.getDayOfWeek().getValue();

        // 1. Obtiene los horarios configurados del médico (en qué días y horas trabaja)
        List<DoctorSchedule> schedules = scheduleRepository.findByDoctorId(doctorId);

        // 2. Trae TODAS las citas que ya existen para ese médico en esa fecha
        List<Appointment> appointmentsOnDate = 
            appointmentRepository.findByDoctorAndDate(doctorId, date.toString());

        // 3. Crea un Set con las horas de inicio ya ocupadas (O(1) para búsquedas)
        Set<LocalTime> occupiedStartTimes = buildOccupiedSet(appointmentsOnDate);

        // 4. Genera los slots basados en el horario y filtra los ocupados
        for (DoctorSchedule schedule : schedules) {
            // Solo procesamos el horario que coincida con el día de la semana de la fecha solicitada
            if (schedule.getDayOfWeek() != dayOfWeek) continue;

            LocalTime cursor = schedule.getStartTime();
            LocalTime endTime = schedule.getEndTime();
            int interval = schedule.getIntervalMinutes();

            // Generamos slots mientras no superemos la hora de fin
            while (cursor.isBefore(endTime)) {
                // Si la hora actual no está en el Set de ocupadas, el slot está libre
                if (!occupiedStartTimes.contains(cursor)) {
                    availableSlots.add(cursor);
                }
                // Avanzamos el cursor según el intervalo (ej: 20 min, 30 min)
                cursor = cursor.plusMinutes(interval);
            }
        }

        return availableSlots;
    }

    @Override
    public int getIntervalMinutesForDoctorOnDate(int doctorId, LocalDate date) {
        int dayOfWeek = date.getDayOfWeek().getValue();
        List<DoctorSchedule> schedules = scheduleRepository.findByDoctorId(doctorId);

        for (DoctorSchedule schedule : schedules) {
            if (schedule.getDayOfWeek() == dayOfWeek) {
                return schedule.getIntervalMinutes();
            }
        }
        // Valor por defecto si no hay horario configurado
        return 30; 
    }

    // Método privado auxiliar para identificar qué horas ya no están disponibles
    private Set<LocalTime> buildOccupiedSet(List<Appointment> appointments) {
        Set<LocalTime> occupied = new HashSet<>();

        if (appointments == null) return occupied;

        for (Appointment appt : appointments) {
            // Las citas CANCELADAS NO ocupan espacio, se ignoran para liberar el slot
            if (appt.getStatus() == AppointmentStatus.CANCELADA) continue;
            
            occupied.add(appt.getStartTime());
        }

        return occupied;
    }
}