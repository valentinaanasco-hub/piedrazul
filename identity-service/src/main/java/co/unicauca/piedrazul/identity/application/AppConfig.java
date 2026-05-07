package co.unicauca.piedrazul.identity.application;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Configuración de beans de la aplicación.
 *
 * @author Santiago Solarte
 */
@Configuration
public class AppConfig {

    // --- BCrypt para encriptar contraseñas ---
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // --- Documentación Swagger ---
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Identity Service API")
                        .description("Servicio de autenticación y gestión de usuarios — Piedrazul")
                        .version("0.1.0"));
    }
}
