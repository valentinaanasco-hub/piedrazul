package co.unicauca.piedrazul.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

/**
 * Filtro global que propaga información del JWT como headers HTTP
 * hacia los microservicios downstream, para que no necesiten
 * validar el token nuevamente.
 *
 * Headers añadidos:
 *   X-User-Id        → subject (Keycloak UUID)
 *   X-User-Username  → preferred_username (email)
 *   X-User-Roles     → roles separados por coma (ADMIN, DOCTOR, ...)
 *   X-User-DbId      → userId custom claim (ID en la BD de identity-service)
 *
 * Nota: NO usamos exchange.getRequest().mutate().header(...) porque en este punto
 * del flujo los headers son de solo lectura (ReadOnlyHttpHeaders) y lanzaría
 * UnsupportedOperationException. En su lugar construimos una copia escribible
 * y la exponemos con un ServerHttpRequestDecorator.
 */
@Component
public class JwtHeaderFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return ReactiveSecurityContextHolder.getContext()
                .map(ctx -> ctx.getAuthentication())
                .filter(auth -> auth instanceof JwtAuthenticationToken)
                .cast(JwtAuthenticationToken.class)
                .map(authToken -> {
                    var jwt = authToken.getToken();

                    String roles = authToken.getAuthorities().stream()
                            .map(GrantedAuthority::getAuthority)
                            .map(a -> a.replace("ROLE_", ""))
                            .collect(Collectors.joining(","));

                    // userId llega como Long de Keycloak; asignar a Object evita
                    // que String.valueOf resuelva la sobrecarga char[].
                    Object userIdClaim = jwt.getClaim("userId");
                    String dbId     = userIdClaim != null ? userIdClaim.toString() : "";
                    String userId   = jwt.getSubject() != null ? jwt.getSubject() : "";
                    String username = jwt.getClaimAsString("preferred_username");
                    if (username == null) username = "";

                    // Copia ESCRIBIBLE de los headers originales + nuestros X-User-*
                    HttpHeaders headers = new HttpHeaders();
                    headers.addAll(exchange.getRequest().getHeaders());
                    headers.set("X-User-Id",       userId);
                    headers.set("X-User-Username", username);
                    headers.set("X-User-Roles",    roles);
                    headers.set("X-User-DbId",     dbId);

                    ServerHttpRequest decorated =
                            new ServerHttpRequestDecorator(exchange.getRequest()) {
                                @Override
                                public HttpHeaders getHeaders() {
                                    return headers;
                                }
                            };

                    return exchange.mutate().request(decorated).build();
                })
                .defaultIfEmpty(exchange)
                .flatMap(chain::filter);
    }

    @Override
    public int getOrder() {
        return -100;
    }
}
