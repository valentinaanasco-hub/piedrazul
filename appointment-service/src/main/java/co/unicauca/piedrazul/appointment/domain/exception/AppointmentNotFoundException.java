package co.unicauca.piedrazul.appointment.domain.exception;

/**
 * Excepción de dominio: la cita solicitada no existe.
 * Mapeada a HTTP 404 por el ExceptionHandler de infraestructura.
 */
public class AppointmentNotFoundException extends RuntimeException {

    public AppointmentNotFoundException(String message) {
        super(message);
    }
}
