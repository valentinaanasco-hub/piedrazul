package co.unicauca.piedrazul.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada del API Gateway.
 * Enruta las peticiones del frontend hacia los microservicios correspondientes
 * y orquesta flujos complejos como el registro de pacientes mediante el patrón Saga.
 *
 * @author Santiago Solarte
 */
@SpringBootApplication
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
