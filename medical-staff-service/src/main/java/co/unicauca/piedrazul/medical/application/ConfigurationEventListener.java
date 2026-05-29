package co.unicauca.piedrazul.medical.application;

import co.unicauca.piedrazul.events.GlobalConfigurationUpdatedEvent;
import co.unicauca.piedrazul.events.ScheduleConfigurationUpdatedEvent;
import co.unicauca.piedrazul.medical.domain.entities.Doctor;
import co.unicauca.piedrazul.medical.domain.entities.DoctorSchedule;
import co.unicauca.piedrazul.medical.domain.repository.DoctorRepository;
import co.unicauca.piedrazul.medical.domain.repository.DoctorScheduleRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Listener de eventos de configuración publicados por configuration-service.
 * Actualiza los horarios de los profesionales cuando se reciben eventos.
 *
 * @author Santiago Solarte
 */
@Component
public class ConfigurationEventListener {

    private final DoctorScheduleRepository scheduleRepository;
    private final DoctorRepository doctorRepository;

    public ConfigurationEventListener(
            DoctorScheduleRepository scheduleRepository,
            DoctorRepository doctorRepository) {
        this.scheduleRepository = scheduleRepository;
        this.doctorRepository = doctorRepository;
    }

    /**
     * Escucha eventos de actualización de horarios de profesionales.
     * Sincroniza los horarios locales con la configuración recibida.
     */
    @Transactional
    @RabbitListener(queues = "piedrazul.schedule.updated.queue")
    public void onScheduleConfigurationUpdated(ScheduleConfigurationUpdatedEvent event) {
        try {
            int doctorId = event.doctorId();
            
            // Verificar que el doctor existe
            Doctor doctor = doctorRepository.findById(doctorId).orElse(null);
            if (doctor == null) {
                System.err.println("[CONFIG-LISTENER] Doctor no encontrado: " + doctorId);
                return;
            }

            // Eliminar horarios anteriores
            scheduleRepository.deleteByDoctorId(doctorId);

            // Si la lista está vacía, significa que se eliminaron todos los horarios
            if (event.schedules().isEmpty()) {
                System.out.println("[CONFIG-LISTENER] Horarios eliminados para doctor: " + doctorId);
                return;
            }

            // Crear nuevos horarios
            List<DoctorSchedule> schedules = new ArrayList<>();
            for (ScheduleConfigurationUpdatedEvent.ScheduleItem item : event.schedules()) {
                DoctorSchedule schedule = new DoctorSchedule();
                schedule.setDoctor(doctor);
                schedule.setDayOfWeek(item.dayOfWeek());
                schedule.setStartTime(item.startTime());
                schedule.setEndTime(item.endTime());
                schedule.setIntervalMinutes(item.intervalMinutes());
                schedules.add(schedule);
            }

            scheduleRepository.saveAll(schedules);
            System.out.println("[CONFIG-LISTENER] Horarios actualizados para doctor: " + doctorId + 
                             " (" + schedules.size() + " días configurados)");

        } catch (Exception e) {
            System.err.println("[CONFIG-LISTENER] Error procesando evento de horarios: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Escucha eventos de actualización de configuración global.
     * Por ahora solo registra el evento (puede extenderse en el futuro).
     */
    @RabbitListener(queues = "piedrazul.global.config.updated.queue")
    public void onGlobalConfigurationUpdated(GlobalConfigurationUpdatedEvent event) {
        System.out.println("[CONFIG-LISTENER] Configuración global actualizada: " + 
                         event.parameterKey() + " = " + event.parameterValue());
        
        // Aquí se puede agregar lógica adicional si medical-staff-service 
        // necesita reaccionar a cambios en la configuración global
    }
}
