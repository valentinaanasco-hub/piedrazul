
package co.unicauca.piedrazul.domain.services.interfaces;

import co.unicauca.piedrazul.domain.entities.Specialty;
import java.util.List;

/**
 *
 * @author santi
 */
public interface ISpecialtyValidator {
    void validateExists(Specialty specialty);
    void validateListNotEmpty(List<Specialty> specialties);
}