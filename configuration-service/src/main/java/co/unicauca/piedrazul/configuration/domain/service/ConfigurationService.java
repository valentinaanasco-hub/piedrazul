package co.unicauca.piedrazul.configuration.domain.service;

import co.unicauca.piedrazul.configuration.application.ConfigurationEventPublisher;
import co.unicauca.piedrazul.configuration.domain.entities.DoctorScheduleConfiguration;
import co.unicauca.piedrazul.configuration.domain.entities.SystemParameter;
import co.unicauca.piedrazul.configuration.domain.repository.DoctorScheduleConfigurationRepository;
import co.unicauca.piedrazul.configuration.domain.repository.SystemParameterRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Servicio de dominio para gestión de configuración del sistema.
 * Maneja tanto la configuración global como la configuración por profesional.
 * Publica eventos asíncronos cuando se actualizan configuraciones.
 *
 * @author Santiago Solarte
 */
@Service
public class ConfigurationService {

    private static final String APPOINTMENT_WINDOW_WEEKS_KEY = "appointment_window_weeks";

    private final SystemParameterRepository systemParameterRepository;
    private final DoctorScheduleConfigurationRepository scheduleRepository;
    private final ConfigurationEventPublisher eventPublisher;

    public ConfigurationService(
            SystemParameterRepository systemParameterRepository,
            DoctorScheduleConfigurationRepository scheduleRepository,
            ConfigurationEventPublisher eventPublisher) {
        this.systemParameterRepository = systemParameterRepository;
        this.scheduleRepository = scheduleRepository;
        this.eventPublisher = eventPublisher;
    }

    // ========== CONFIGURACIÓN GLOBAL ==========

    /**
     * Obtiene la ventana de tiempo en semanas para agendar citas.
     * Por defecto retorna 4 semanas si no está configurado.
     */
    public int getAppointmentWindowWeeks() {
        return systemParameterRepository.findById(APPOINTMENT_WINDOW_WEEKS_KEY)
                .map(param -> Integer.parseInt(param.getValue()))
                .orElse(4);
    }

    /**
     * Actualiza la ventana de tiempo en semanas para agendar citas.
     * Publica un evento asíncrono para notificar a otros servicios.
     */
    @Transactional
    public void updateAppointmentWindowWeeks(int weeks) {
        if (weeks < 1 || weeks > 52) {
            throw new IllegalArgumentException("La ventana de tiempo debe estar entre 1 y 52 semanas");
        }

        SystemParameter parameter = systemParameterRepository
                .findById(APPOINTMENT_WINDOW_WEEKS_KEY)
                .orElse(new SystemParameter(
                        APPOINTMENT_WINDOW_WEEKS_KEY,
                        String.valueOf(weeks),
                        "Ventana de tiempo en semanas para agendar citas"
                ));

        parameter.setValue(String.valueOf(weeks));
        systemParameterRepository.save(parameter);

        // Publicar evento asíncrono
        eventPublisher.publishGlobalConfigUpdated(APPOINTMENT_WINDOW_WEEKS_KEY, String.valueOf(weeks));
    }

    // ========== CONFIGURACIÓN POR PROFESIONAL ==========

    /**
     * Obtiene la configuración de horarios de un profesional.
     */
    public List<DoctorScheduleConfiguration> getDoctorSchedule(int doctorId) {
        return scheduleRepository.findByDoctorId(doctorId);
    }

    /**
     * Actualiza la configuración de horarios de un profesional.
     * Elimina los horarios anteriores, crea los nuevos y publica un evento asíncrono.
     */
    @Transactional
    public List<DoctorScheduleConfiguration> updateDoctorSchedule(int doctorId, 
                                                                    List<DoctorScheduleConfiguration> newSchedules) {
        // Eliminar horarios anteriores y hacer flush para que se ejecute inmediatamente
        scheduleRepository.deleteByDoctorId(doctorId);
        scheduleRepository.flush();

        // Guardar nuevos horarios
        newSchedules.forEach(schedule -> schedule.setDoctorId(doctorId));
        List<DoctorScheduleConfiguration> saved = scheduleRepository.saveAll(newSchedules);

        // Publicar evento asíncrono
        eventPublisher.publishScheduleUpdated(doctorId, saved);

        return saved;
    }

    /**
     * Elimina todos los horarios de un profesional.
     */
    @Transactional
    public void deleteDoctorSchedule(int doctorId) {
        scheduleRepository.deleteByDoctorId(doctorId);
        
        // Publicar evento con lista vacía para indicar eliminación
        eventPublisher.publishScheduleUpdated(doctorId, List.of());
    }
}
