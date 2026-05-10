package co.unicauca.piedrazul.medical.domain.factory;

/**
 * Creator abstracto del patrón Factory Method.
 * Define el método de fábrica que las subclases deben implementar
 * para crear el generador de disponibilidad apropiado.
 * Cada subclase decide qué tipo de generador crear.
 */
public abstract class AvailabilityGeneratorFactory {

    /**
     * Factory Method — cada subclase implementa este método
     * para retornar el generador de disponibilidad correspondiente
     */
    public abstract AvailabilityGenerator createGenerator();

    /**
     * Método de negocio que usa el generador creado por la subclase.
     * El creator no sabe qué implementación concreta se usará.
     */
    public AvailabilityGenerator getGenerator() {
        return createGenerator();
    }
}
