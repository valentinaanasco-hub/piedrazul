package co.unicauca.piedrazul.appointment.application;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de beans de la aplicación
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
}
