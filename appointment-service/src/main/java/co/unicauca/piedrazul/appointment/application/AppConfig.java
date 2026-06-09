package co.unicauca.piedrazul.appointment.application;

import co.unicauca.piedrazul.appointment.domain.port.out.AppointmentEventPort;
import co.unicauca.piedrazul.appointment.domain.port.out.AppointmentRepositoryPort;
import co.unicauca.piedrazul.appointment.domain.port.out.AppointmentWindowPort;
import co.unicauca.piedrazul.appointment.domain.port.out.PatientAuthorizationPort;
import co.unicauca.piedrazul.appointment.domain.port.out.UserValidationPort;
import co.unicauca.piedrazul.appointment.domain.service.ColombianHolidaysService;
import co.unicauca.piedrazul.appointment.domain.service.builder.AppointmentDirector;
import co.unicauca.piedrazul.appointment.domain.service.template.ManualAppointmentScheduling;
import co.unicauca.piedrazul.appointment.domain.service.template.RescheduleAppointmentScheduling;
import co.unicauca.piedrazul.appointment.domain.service.validator.ActiveAppointmentValidator;
import co.unicauca.piedrazul.appointment.domain.service.validator.AppointmentValidator;
import co.unicauca.piedrazul.appointment.domain.service.validator.AppointmentWindowValidator;
import co.unicauca.piedrazul.appointment.domain.service.validator.ConflictAppointmentValidator;
import co.unicauca.piedrazul.appointment.domain.service.validator.DataAppointmentValidator;
import co.unicauca.piedrazul.appointment.domain.service.validator.ExistenceAppointmentValidator;
import co.unicauca.piedrazul.appointment.domain.service.validator.HolidayValidator;
import co.unicauca.piedrazul.appointment.domain.service.validator.MedicinaGeneralValidator;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * Capa de aplicación: instancia y ensambla los objetos de dominio.
 * Los POJOs de dominio no tienen anotaciones de Spring; esta clase los registra como beans.
 *
 * Cadena de validadores (Chain of Responsibility):
 *  1. DataAppointmentValidator      — datos básicos
 *  2. HolidayValidator              — no festivos
 *  3. ExistenceAppointmentValidator — paciente y médico existen y activos
 *  4. ActiveAppointmentValidator    — máximo 1 cita activa por paciente
 *  5. MedicinaGeneralValidator      — paso previo por Consulta General
 *  6. ConflictAppointmentValidator  — sin conflicto de horario
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

    @Bean
    public ColombianHolidaysService colombianHolidaysService() {
        return new ColombianHolidaysService();
    }

    // Builder
    @Bean
    public AppointmentDirector appointmentDirector() {
        return new AppointmentDirector();
    }

    // Validadores
    @Bean
    public DataAppointmentValidator dataAppointmentValidator() {
        return new DataAppointmentValidator();
    }

    @Bean
    public HolidayValidator holidayValidator(ColombianHolidaysService holidaysService) {
        return new HolidayValidator(holidaysService);
    }

    @Bean
    public ExistenceAppointmentValidator existenceAppointmentValidator(UserValidationPort userValidationPort) {
        return new ExistenceAppointmentValidator(userValidationPort);
    }

    @Bean
    public ActiveAppointmentValidator activeAppointmentValidator(AppointmentRepositoryPort repositoryPort) {
        return new ActiveAppointmentValidator(repositoryPort);
    }

    @Bean
    public MedicinaGeneralValidator medicinaGeneralValidator(PatientAuthorizationPort authorizationPort) {
        return new MedicinaGeneralValidator(authorizationPort);
    }

    @Bean
    public AppointmentWindowValidator appointmentWindowValidator(AppointmentWindowPort windowPort) {
        return new AppointmentWindowValidator(windowPort);
    }

    @Bean
    public ConflictAppointmentValidator conflictAppointmentValidator() {
        return new ConflictAppointmentValidator();
    }

    @Bean
    public List<AppointmentValidator> appointmentValidators(
            DataAppointmentValidator dataValidator,
            HolidayValidator holidayValidator,
            ExistenceAppointmentValidator existenceValidator,
            ActiveAppointmentValidator activeValidator,
            AppointmentWindowValidator appointmentWindowValidator,
            MedicinaGeneralValidator medicinaGeneralValidator,
            ConflictAppointmentValidator conflictValidator) {

        return List.of(
                dataValidator,
                holidayValidator,
                existenceValidator,
                activeValidator,
                appointmentWindowValidator,
                medicinaGeneralValidator,
                conflictValidator
        );
    }

    // Template Method
    @Bean
    public ManualAppointmentScheduling manualAppointmentScheduling(
            AppointmentRepositoryPort repositoryPort,
            List<AppointmentValidator> validators,
            AppointmentEventPort eventPort) {
        return new ManualAppointmentScheduling(repositoryPort, validators, eventPort);
    }

    @Bean
    public RescheduleAppointmentScheduling rescheduleAppointmentScheduling(
            AppointmentRepositoryPort repositoryPort,
            List<AppointmentValidator> validators,
            AppointmentEventPort eventPort) {
        return new RescheduleAppointmentScheduling(repositoryPort, validators, eventPort);
    }
}
