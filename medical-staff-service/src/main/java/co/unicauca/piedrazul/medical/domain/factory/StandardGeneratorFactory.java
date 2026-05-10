package co.unicauca.piedrazul.medical.domain.factory;

import org.springframework.stereotype.Component;

/**
 * Concrete Creator del patrón Factory Method.
 * Crea generadores de disponibilidad estándar para médicos
 * con horarios regulares de lunes a viernes.
 */
@Component
public class StandardGeneratorFactory extends AvailabilityGeneratorFactory {

    @Override
    public AvailabilityGenerator createGenerator() {
        return new StandardAvailabilityGenerator();
    }
}
