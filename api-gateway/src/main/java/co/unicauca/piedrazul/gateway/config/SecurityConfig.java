package co.unicauca.piedrazul.gateway.config;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverter;
import org.springframework.security.web.server.SecurityWebFilterChain;

import reactor.core.publisher.Flux;

/**
 * Seguridad del API Gateway: valida JWT emitidos por Keycloak.
 * Rutas públicas (login, registro, docs) no requieren token.
 * Todo lo demás exige Bearer token válido.
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    private static final Set<String> APP_ROLES =
            Set.of("ADMIN", "DOCTOR", "PACIENTE", "AGENDADOR");

    @Bean
    public SecurityWebFilterChain securityFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)

                // CORS lo maneja el CorsWebFilter de GatewayConfig
                .cors(Customizer.withDefaults())

                .authorizeExchange(auth -> auth
                        // Preflight CORS
                        .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Autenticación: login legacy + registro público
                        .pathMatchers(HttpMethod.POST, "/api/v1/identity/login").permitAll()
                        .pathMatchers(HttpMethod.POST, "/api/v1/register/**").permitAll()
                        .pathMatchers(HttpMethod.POST, "/api/v1/identity/register").permitAll()

                        // Documentación Swagger
                        .pathMatchers("/swagger-ui/**", "/api-docs/**", "/webjars/**").permitAll()
                        .pathMatchers("/v3/api-docs/**").permitAll()

                        // ── CONFIGURACIÓN ──
                        // Lectura de configuración: accesible para todo el personal
                        .pathMatchers(HttpMethod.GET, "/api/v1/configuration/**").hasAnyRole("ADMIN", "DOCTOR", "AGENDADOR")
                        // Escritura: solo ADMIN
                        .pathMatchers("/api/v1/configuration/**").hasRole("ADMIN")

                        // ── MÉDICOS ──
                        .pathMatchers(HttpMethod.GET, "/api/v1/medical/doctors/**").hasAnyRole("DOCTOR", "ADMIN", "AGENDADOR", "PACIENTE")
                        .pathMatchers(HttpMethod.POST, "/api/v1/medical/doctors/**").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.PUT, "/api/v1/medical/doctors/**").hasAnyRole("ADMIN", "DOCTOR")
                        .pathMatchers(HttpMethod.DELETE, "/api/v1/medical/doctors/**").hasRole("ADMIN")
                        .pathMatchers("/api/v1/medical/**").hasAnyRole("DOCTOR", "ADMIN", "AGENDADOR", "PACIENTE")

                        // ── PACIENTES ──
                        .pathMatchers(HttpMethod.GET, "/api/v1/patients/**").hasAnyRole("PACIENTE", "DOCTOR", "ADMIN", "AGENDADOR")
                        .pathMatchers(HttpMethod.POST, "/api/v1/patients/register/web").permitAll()
                        .pathMatchers(HttpMethod.POST, "/api/v1/patients/**").hasAnyRole("ADMIN", "AGENDADOR", "DOCTOR")
                        .pathMatchers(HttpMethod.PUT, "/api/v1/patients/**").hasAnyRole("PACIENTE", "ADMIN")
                        .pathMatchers(HttpMethod.DELETE, "/api/v1/patients/**").hasRole("ADMIN")

                        // ── CITAS ──
                        .pathMatchers(HttpMethod.POST, "/api/v1/appointments").hasAnyRole("PACIENTE", "AGENDADOR", "ADMIN", "DOCTOR")
                        .pathMatchers(HttpMethod.GET, "/api/v1/appointments/patient/**").hasAnyRole("PACIENTE", "DOCTOR", "ADMIN")
                        .pathMatchers(HttpMethod.GET, "/api/v1/appointments/doctor/**").hasAnyRole("DOCTOR", "ADMIN", "AGENDADOR", "PACIENTE")
                        .pathMatchers(HttpMethod.GET, "/api/v1/appointments/**").hasAnyRole("DOCTOR", "ADMIN", "AGENDADOR")
                        .pathMatchers(HttpMethod.PATCH, "/api/v1/appointments/*/cancel").hasAnyRole("PACIENTE", "AGENDADOR", "DOCTOR", "ADMIN")
                        .pathMatchers(HttpMethod.PATCH, "/api/v1/appointments/*/attend").hasAnyRole("DOCTOR", "AGENDADOR", "ADMIN")
                        .pathMatchers("/api/v1/appointments/**").hasAnyRole("DOCTOR", "ADMIN", "AGENDADOR", "PACIENTE")

                        // ── IDENTIDAD ──
                        // Consulta de usuarios (nombre, etc.) para mostrarlos en la UI del personal
                        .pathMatchers(HttpMethod.GET, "/api/v1/identity/users/**").hasAnyRole("DOCTOR", "AGENDADOR", "ADMIN", "PACIENTE")
                        // El resto de identidad, solo ADMIN
                        .pathMatchers("/api/v1/identity/**").hasRole("ADMIN")

                        // Todo lo demás requiere autenticación
                        .anyExchange().authenticated()
                )

                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(keycloakJwtConverter()))
                )

                .build();
    }

    /**
     * Extrae los roles del claim realm_access.roles de Keycloak
     * y los convierte en GrantedAuthority con prefijo ROLE_.
     */
    @Bean
    public ReactiveJwtAuthenticationConverter keycloakJwtConverter() {
        var converter = new ReactiveJwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
            if (realmAccess == null || !realmAccess.containsKey("roles")) {
                return Flux.empty();
            }

            @SuppressWarnings("unchecked")
            List<String> roles = (List<String>) realmAccess.get("roles");

            return Flux.fromIterable(roles)
                    .filter(APP_ROLES::contains)
                    .map(role -> (GrantedAuthority) new SimpleGrantedAuthority("ROLE_" + role));
        });

        return converter;
    }
}
