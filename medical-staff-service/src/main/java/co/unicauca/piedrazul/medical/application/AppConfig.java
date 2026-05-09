package co.unicauca.piedrazul.medical.application;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de beans de la aplicación.
 *
 * @author Ginner Ortega
 */
@Configuration
public class AppConfig {

    // --- Swagger ---
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Medical Staff Service API")
                        .description("Servicio de gestión del personal médico — Piedrazul")
                        .version("0.1.0"));
    }

    // --- RabbitMQ: cola para eventos de citas ---
    @Bean
    public Queue appointmentEventsQueue() {
        return new Queue("appointment.events", true);
    }
}
