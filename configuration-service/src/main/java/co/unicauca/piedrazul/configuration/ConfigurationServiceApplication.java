package co.unicauca.piedrazul.configuration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada del Configuration Service.
 * Microservicio responsable de gestionar la configuración del sistema.
 *
 * @author Santiago Solarte
 */
@SpringBootApplication
public class ConfigurationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConfigurationServiceApplication.class, args);
    }
}
