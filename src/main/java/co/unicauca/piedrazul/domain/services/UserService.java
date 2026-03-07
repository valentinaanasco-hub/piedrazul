package co.unicauca.piedrazul.domain.services;

import co.unicauca.piedrazul.domain.access.IUserRepository;
import co.unicauca.piedrazul.domain.model.User;
import org.mindrot.jbcrypt.BCrypt;
import java.util.regex.Pattern;

/**
 *
 * @author Santiago Solarte
 */
public class UserService {

    private final IUserRepository repository;

    // Inyección de Dependencia por constructor (DIP)
    public UserService(IUserRepository repository) {
        this.repository = repository;
    }

    /**
     * Valida y registra un usuario en el sistema.
     * Cumple con los requisitos de la tarea: validación de contraseña y cifrado.
     */
    public String registerUser(User user, String plainPassword) {
        // 1. Validar reglas de la contraseña
        if (!validatePasswordStrength(plainPassword)) {
            return "La contraseña no cumple los requisitos: Mínimo 6 caracteres, "
                 + "un dígito, una mayúscula y un carácter especial.";
        }

        // 2. Verificar si el login ya existe
        if (repository.findByUsername(user.getUsername()) != null) {
            return "El nombre de usuario ya existe.";
        }

        // 3. Cifrar la contraseña antes de mandarla al repositorio
        String hashedPassword = BCrypt.hashpw(plainPassword, BCrypt.gensalt());
        user.setPassword(hashedPassword);

        // 4. Guardar
        boolean success = repository.save(user);
        return success ? "SUCCESS" : "Error al guardar en la base de datos.";
    }

    /**
     * Valida el inicio de sesión
     */
    public User login(String username, String plainPassword) {
        User user = repository.findByUsername(username);
        
        if (user != null && BCrypt.checkpw(plainPassword, user.getPassword())) {
            return user; // Credenciales correctas
        }
        return null; // Usuario no encontrado o contraseña incorrecta
    }

    /**
     * Regex para cumplir la tarea: 
     * Mínimo 6 caracteres, 1 dígito, 1 especial, 1 mayúscula.
     */
    private boolean validatePasswordStrength(String password) {
        if (password == null || password.length() < 6) return false;
        
        String regex = "^(?=.*[0-9])(?=.*[A-Z])(?=.*[!@#$%^&*(),.?\":{}|<>]).{6,}$";
        return Pattern.compile(regex).matcher(password).matches();
    }
}