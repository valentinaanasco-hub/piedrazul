package co.unicauca.piedrazul.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Configuración de RestTemplate para llamadas síncronas a microservicios
 * Usado por la Saga para coordinar identity-service y patient-service
 */
@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
