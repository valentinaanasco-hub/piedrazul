package co.unicauca.piedrazul.identity.application;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
public class AppConfig {

    @Value("${keycloak.admin.server-url:http://localhost:8180}")
    private String keycloakServerUrl;

    @Value("${keycloak.admin.realm:piedrazul}")
    private String keycloakRealm;

    @Value("${keycloak.admin.username:admin}")
    private String keycloakAdminUsername;

    @Value("${keycloak.admin.password:admin}")
    private String keycloakAdminPassword;

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Cliente admin de Keycloak autenticado con las credenciales del realm master.
     * Usado por KeycloakAdminAdapter para crear/deshabilitar usuarios.
     */
    @Bean
    public Keycloak keycloakAdmin() {
        return KeycloakBuilder.builder()
                .serverUrl(keycloakServerUrl)
                .realm("master")
                .clientId("admin-cli")
                .username(keycloakAdminUsername)
                .password(keycloakAdminPassword)
                .build();
    }

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Identity Service API")
                        .description("Servicio de autenticación y gestión de usuarios — Piedrazul")
                        .version("0.1.0"));
    }
}
