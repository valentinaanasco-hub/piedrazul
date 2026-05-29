package co.unicauca.piedrazul.appointment.application;

import co.unicauca.piedrazul.appointment.domain.validator.ActiveAppointmentValidator;
import co.unicauca.piedrazul.appointment.domain.validator.AppointmentValidator;
import co.unicauca.piedrazul.appointment.domain.validator.ConflictAppointmentValidator;
import co.unicauca.piedrazul.appointment.domain.validator.DataAppointmentValidator;
import co.unicauca.piedrazul.appointment.domain.validator.HolidayValidator;
import co.unicauca.piedrazul.appointment.domain.validator.MedicinaGeneralValidator;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

/**
 * Configuración de beans de la aplicación
 * Define la cadena de validadores (Chain of Responsibility)
 */
@Configuration
public class AppConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Appointment Service API")
                        .description("Servicio de gestión de citas médicas — Piedrazul")
                        .version("0.1.0"));
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    /**
     * Cadena de validadores para citas médicas
     * Orden de ejecución:
     * 1. DataAppointmentValidator - Valida datos básicos
     * 2. HolidayValidator - Valida que no sea festivo
     * 3. ActiveAppointmentValidator - Valida límite de 1 cita activa
     * 4. MedicinaGeneralValidator - Valida que haya pasado por Medicina General
     * 5. ConflictAppointmentValidator - Valida conflictos de horario
     */
    @Bean
    public List<AppointmentValidator> appointmentValidators(
            DataAppointmentValidator dataValidator,
            HolidayValidator holidayValidator,
            ActiveAppointmentValidator activeAppointmentValidator,
            MedicinaGeneralValidator medicinaGeneralValidator,
            ConflictAppointmentValidator conflictValidator) {
        
        return Arrays.asList(
            dataValidator,
            holidayValidator,
            activeAppointmentValidator,
            medicinaGeneralValidator,
            conflictValidator
        );
    }
}
