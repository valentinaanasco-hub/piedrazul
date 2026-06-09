package co.unicauca.piedrazul.appointment.domain.exception;

/**
 * Excepción de dominio: datos inválidos o prerequisito de negocio no cumplido.
 * Mapeada a HTTP 422 por el ExceptionHandler de infraestructura.
 */
public class AppointmentValidationException extends RuntimeException {

    public AppointmentValidationException(String message) {
        super(message);
    }
}
