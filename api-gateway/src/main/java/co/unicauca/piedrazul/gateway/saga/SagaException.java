package co.unicauca.piedrazul.gateway.saga;

/**
 * Excepción lanzada cuando una saga falla y no puede completarse.
 *
 * @author Santiago Solarte
 */
public class SagaException extends RuntimeException {

    public SagaException(String message) {
        super(message);
    }
}
