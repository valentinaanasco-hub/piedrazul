
package co.unicauca.piedrazul.domain.services.interfaces;

import co.unicauca.piedrazul.domain.entities.User;

/**
 *
 * @author santi
 */
public interface IUserValidator {
    void validateUser(User user);
    void validateExists(User user);
    public void validateUserName(String username);
    public void validatePassword(String password);
}