package co.unicauca.piedrazul.presentation.controllers;

import co.unicauca.piedrazul.domain.entities.User;
import co.unicauca.piedrazul.domain.services.interfaces.IUserService;
import java.util.ArrayList;
import java.util.List;

public class UserController {
    private final IUserService userService;
    private String lastErrorMessage;

    public UserController(IUserService userService) {
        this.userService = userService;
    }

    // Crea un nuevo usuario validando que no existan duplicados
    public boolean createAccount(User user) {
        try {
            lastErrorMessage = null;
            return userService.registerUser(user);
        } catch (IllegalArgumentException e) {
            lastErrorMessage = e.getMessage();
            return false;
        }
    }

    // Autentica al usuario y retorna su perfil si las credenciales son correctas
    public User authenticate(String username, String password) {
        try {
            lastErrorMessage = null;
            return userService.login(username, password);
        } catch (IllegalArgumentException e) {
            lastErrorMessage = e.getMessage();
            return null;
        }
    }

    // Obtiene el listado completo de usuarios del sistema
    public List<User> getAllUsers() {
        try {
            lastErrorMessage = null;
            return userService.listUsers();
        } catch (Exception e) {
            lastErrorMessage = e.getMessage();
            return new ArrayList<>();
        }
    }

    // Localiza un usuario por su nombre de cuenta
    public User getByUsername(String username) {
        try {
            lastErrorMessage = null;
            return userService.findUser(username);
        } catch (IllegalArgumentException e) {
            lastErrorMessage = e.getMessage();
            return null;
        }
    }

    // Actualiza los datos generales del usuario
    public boolean updateProfile(User user) {
        try {
            lastErrorMessage = null;
            return userService.modifyUser(user);
        } catch (IllegalArgumentException e) {
            lastErrorMessage = e.getMessage();
            return false;
        }
    }

    // Inactiva el acceso de un usuario al sistema
    public boolean disableAccount(int id) {
        try {
            lastErrorMessage = null;
            return userService.deactivateUser(id);
        } catch (IllegalArgumentException e) {
            lastErrorMessage = e.getMessage();
            return false;
        }
    }

    public String getLastErrorMessage() {
        return lastErrorMessage;
    }
}