package co.unicauca.piedrazul.domain.access;

import co.unicauca.piedrazul.domain.entities.SystemParameter;

/**
 *
 * @author santi
 */
public interface ISystemParameterRepository {

    SystemParameter findByKey(String key);
}
