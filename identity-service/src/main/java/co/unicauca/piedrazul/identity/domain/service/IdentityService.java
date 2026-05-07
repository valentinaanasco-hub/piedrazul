package co.unicauca.piedrazul.identity.domain.service;

import co.unicauca.piedrazul.identity.domain.entities.Role;
import co.unicauca.piedrazul.identity.domain.entities.User;
import co.unicauca.piedrazul.identity.domain.enums.UserState;
import co.unicauca.piedrazul.identity.domain.repository.RoleRepository;
import co.unicauca.piedrazul.identity.domain.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Servicio de dominio para gestión de identidad.
 * Maneja autenticación, registro y consulta de usuarios.
 *
 * @author Santiago Solarte
 */
@Service
public class IdentityService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public IdentityService(UserRepository userRepository,
                           RoleRepository roleRepository,
                           BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Autentica a un usuario verificando sus credenciales.
     * Acepta correo electrónico o nombre de usuario.
     */
    public User login(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            // Compatibilidad con contraseñas en texto plano del monolito
            if (!password.equals(user.getPassword())) {
                throw new IllegalArgumentException("Contraseña incorrecta");
            }
        }

        if (user.getState() == UserState.INACTIVO) {
            throw new IllegalArgumentException("Usuario inactivo");
        }

        return user;
    }

    /**
     * Registra un nuevo usuario en el sistema.
     * Encripta la contraseña con BCrypt antes de guardar.
     */
    @Transactional
    public User register(User user, String roleName) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new IllegalArgumentException("El correo ya está registrado en el sistema");
        }

        // Encriptar contraseña
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Asignar rol
        Role role = roleRepository.findByRoleName(roleName)
                .orElseThrow(() -> new IllegalArgumentException("Rol no encontrado: " + roleName));
        user.setRoles(List.of(role));

        return userRepository.save(user);
    }

    /**
     * Busca un usuario por su ID.
     */
    public User findById(int id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
    }

    /**
     * Busca un usuario por su username.
     */
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
    }

    /**
     * Lista todos los usuarios del sistema.
     */
    public List<User> listAll() {
        return userRepository.findAll();
    }

    /**
     * Desactiva un usuario sin eliminarlo.
     */
    @Transactional
    public void deactivate(int id) {
        User user = findById(id);
        user.setState(UserState.INACTIVO);
        userRepository.save(user);
    }
}
