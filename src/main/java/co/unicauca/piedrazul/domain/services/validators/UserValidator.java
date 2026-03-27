package co.unicauca.piedrazul.domain.services.validators;

import co.unicauca.piedrazul.domain.entities.Role;
import co.unicauca.piedrazul.domain.entities.User;
import co.unicauca.piedrazul.domain.services.interfaces.IUserValidator;
import java.util.List;

public class UserValidator implements IUserValidator{

    @Override
    public void validateUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("El objeto usuario no puede ser nulo");
        }
        validateUserName(user.getUsername());
        validatePassword(user.getPassword());
        validateNames(user.getFirstName(), user.getFirstSurname());
        validateRoles(user.getRoles());
    }
    
    @Override
    public void validateUserName(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de usuario es obligatorio");
        }
    }

    @Override
    public void validatePassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("La contraseña es obligatoria");
        }
    }

    private void validateNames(String firstName, String lastName) {
        if (firstName == null || firstName.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
        if (lastName == null || lastName.trim().isEmpty()) {
            throw new IllegalArgumentException("El apellido es obligatorio");
        }
    }

    private void validateRoles(List<Role> roles) {
        if (roles == null || roles.isEmpty()) {
            throw new IllegalArgumentException("El usuario debe tener al menos un rol");
        }
    }
    
    @Override
    public void validateExists(User user) {
        if (user == null) {
            throw new IllegalArgumentException("El usuario solicitado no existe.");
        }
    }
}
