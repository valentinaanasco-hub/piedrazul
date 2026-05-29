package co.unicauca.piedrazul.configuration.application;

import co.unicauca.piedrazul.configuration.domain.entities.DoctorScheduleConfiguration;
import co.unicauca.piedrazul.configuration.domain.repository.DoctorScheduleConfigurationRepository;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Publica eventos de configuración existentes al iniciar el servicio
 * Esto asegura que medical-staff-service tenga los horarios sincronizados
 */
@Component
public class StartupEventPublisher {

    private final DoctorScheduleConfigurationRepository scheduleRepository;
    private final ConfigurationEventPublisher eventPublisher;

    public StartupEventPublisher(
            DoctorScheduleConfigurationRepository scheduleRepository,
            ConfigurationEventPublisher eventPublisher) {
        this.scheduleRepository = scheduleRepository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Se ejecuta cuando la aplicación está lista
     * Publica eventos de todos los horarios existentes
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        System.out.println("[STARTUP-PUBLISHER] Iniciando sincronización de horarios existentes...");

        try {
            // Obtener todos los horarios
            List<DoctorScheduleConfiguration> allSchedules = scheduleRepository.findAll();

            if (allSchedules.isEmpty()) {
                System.out.println("[STARTUP-PUBLISHER] No hay horarios configurados para sincronizar");
                return;
            }

            // Agrupar por doctor
            Map<Integer, List<DoctorScheduleConfiguration>> schedulesByDoctor = allSchedules.stream()
                    .collect(Collectors.groupingBy(DoctorScheduleConfiguration::getDoctorId));

            // Publicar evento para cada doctor
            for (Map.Entry<Integer, List<DoctorScheduleConfiguration>> entry : schedulesByDoctor.entrySet()) {
                int doctorId = entry.getKey();
                List<DoctorScheduleConfiguration> schedules = entry.getValue();

                System.out.println("[STARTUP-PUBLISHER] Publicando horarios para doctor: " + doctorId + 
                                 " (" + schedules.size() + " días configurados)");

                eventPublisher.publishScheduleUpdated(doctorId, schedules);
            }

            System.out.println("[STARTUP-PUBLISHER] Sincronización completada. Total doctores: " + 
                             schedulesByDoctor.size());

        } catch (Exception e) {
            System.err.println("[STARTUP-PUBLISHER] Error al sincronizar horarios: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
