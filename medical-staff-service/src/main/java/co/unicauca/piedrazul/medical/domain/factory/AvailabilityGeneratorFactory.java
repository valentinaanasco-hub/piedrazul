package co.unicauca.piedrazul.medical.domain.factory;

import org.springframework.stereotype.Component;

/**
 * Fábrica del patrón Factory Method.
 * Decide qué generador de disponibilidad usar según el contexto.
 * Actualmente retorna el generador estándar; en el futuro podría
 * retornar generadores especializados (urgencias, telemedicina, etc).
 *
 * Contexto: patrón Factory Method — creator concreto.
 *
 * @author Ginner Ortega
 */
@Component
public class AvailabilityGeneratorFactory {

    private final StandardAvailabilityGenerator standardGenerator;

    public AvailabilityGeneratorFactory(StandardAvailabilityGenerator standardGenerator) {
        this.standardGenerator = standardGenerator;
    }

    /**
     * Retorna el generador apropiado según el tipo de horario.
     *
     * @param scheduleType Tipo de horario (STANDARD por defecto).
     * @return Generador de disponibilidad.
     */
    public AvailabilityGenerator getGenerator(String scheduleType) {
        // Extensible: si en el futuro hay tipos especiales (urgencias, telemedicina)
        // se agregan aquí sin modificar el código existente
        return standardGenerator;
    }
}
