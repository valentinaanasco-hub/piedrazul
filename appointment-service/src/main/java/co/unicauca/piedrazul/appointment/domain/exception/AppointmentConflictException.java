package co.unicauca.piedrazul.appointment.domain.exception;

/**
 * Excepción de dominio: conflicto de negocio (horario ocupado, estado inválido, etc.).
 * Mapeada a HTTP 409 por el ExceptionHandler de infraestructura.
 */
public class AppointmentConflictException extends RuntimeException {

    public AppointmentConflictException(String message) {
        super(message);
    }
}
