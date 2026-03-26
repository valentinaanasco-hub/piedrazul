package co.unicauca.piedrazul.domain.services;

import co.unicauca.piedrazul.domain.entities.User;
import java.util.List;
import co.unicauca.piedrazul.domain.access.IUserRepository;
import co.unicauca.piedrazul.domain.entities.enums.UserState;
import co.unicauca.piedrazul.domain.services.interfaces.IUserService;
import co.unicauca.piedrazul.domain.services.interfaces.IUserValidator;

/**
 * @author Valentina Añasco 
 * @author Camila Dorado
 * @author Felipe Gutierrez
 * @author Ginner Ortega
 * @author Santiago Solarte 
 */

public class UserService implements IUserService {
    private final IUserRepository userRepository;
    private final IUserValidator validator; // Inyectamos la abstracción

    // Inyección de dependencias 
    public UserService(IUserRepository userRepository, IUserValidator validator) {
        this.userRepository = userRepository;
        this.validator = validator;
    }

    @Override
    public boolean registerUser(User user) {
        // Validamos formato y campos obligatorios
        User userDuplicateUserName = findUser(user.getUsername());
        validator.validateExists(userDuplicateUserName);
        validator.validateUser(user);

        // Validamos lógica de negocio (regla de unicidad)
        if (userRepository.findByUsername(user.getUsername()) != null) {
            throw new IllegalArgumentException("El nombre de usuario ya existe");
        }

        return userRepository.save(user);
    }

    @Override
    public User login(String username, String password) {
        // Validamos que los parámetros de entrada no sean nulos/vacíos
        /*falta validar id*/
        validator.validateUserName(username);
        validator.validatePassword(password);
        

        User user = userRepository.findByUsername(username);
        
        // Delegamos la validación de existencia
        validator.validateExists(user);

        // Verificamos credenciales
        if (!user.getPassword().equals(password)) {
            throw new IllegalArgumentException("Contraseña incorrecta");
        }

        // Verificamos estado
        if (user.getState().equals(UserState.INACTIVO)) {
            throw new IllegalArgumentException("Usuario inactivo");
        }

        return user;
    }

    @Override
    public List<User> listUsers() {
        return userRepository.findAll();
    }

    @Override
    public User findUser(String username) {
        User user = userRepository.findByUsername(username);
        validator.validateExists(user); 
        return user;
    }

    @Override
    public boolean modifyUser(User user) {
        // Validamos que el usuario que viene por parámetro sea válido
        validator.validateUser(user);
        
        // Verificamos que ya exista en la base de datos
        User existing = userRepository.findByUsername(user.getUsername());
        validator.validateExists(existing);
        
        return userRepository.update(user);
    }

    @Override
    public boolean deactivateUser(int id) {
        return userRepository.deactivate(id);
    }
}
