package co.unicauca.piedrazul.identity.domain.service;

import co.unicauca.piedrazul.identity.application.UserEventPublisher;
import co.unicauca.piedrazul.identity.domain.entities.Role;
import co.unicauca.piedrazul.identity.domain.entities.User;
import co.unicauca.piedrazul.identity.domain.enums.UserState;
import co.unicauca.piedrazul.identity.domain.repository.RoleRepository;
import co.unicauca.piedrazul.identity.domain.repository.UserRepository;
import co.unicauca.piedrazul.identity.infrastructure.keycloak.KeycloakAdminAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class IdentityService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final UserEventPublisher eventPublisher;
    private final KeycloakAdminAdapter keycloakAdmin;

    public IdentityService(UserRepository userRepository,
                           RoleRepository roleRepository,
                           BCryptPasswordEncoder passwordEncoder,
                           UserEventPublisher eventPublisher,
                           KeycloakAdminAdapter keycloakAdmin) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.eventPublisher = eventPublisher;
        this.keycloakAdmin = keycloakAdmin;
    }

    public User login(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            if (!password.equals(user.getPassword())) {
                throw new IllegalArgumentException("Contraseña incorrecta");
            }
        }

        if (user.getState() == UserState.INACTIVO) {
            throw new IllegalArgumentException("Usuario inactivo");
        }

        return user;
    }

    @Transactional
    public User register(User user, String roleName) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new IllegalArgumentException("El correo ya está registrado en el sistema");
        }

        // Capturar contraseña en texto plano antes de encriptar (Keycloak la necesita)
        String plainPassword = user.getPassword();
        user.setPassword(passwordEncoder.encode(plainPassword));

        Role role = roleRepository.findByRoleName(roleName)
                .orElseThrow(() -> new IllegalArgumentException("Rol no encontrado: " + roleName));
        user.setRoles(List.of(role));

        User saved = userRepository.save(user);

        // Crear usuario en Keycloak para que pueda autenticarse con OAuth2
        keycloakAdmin.createUser(saved, plainPassword, roleName);

        eventPublisher.publishUserRegistered(saved);
        return saved;
    }

    public User findById(long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
    }

    public List<User> listAll() {
        return userRepository.findAll();
    }

    @Transactional
    public void deactivate(long id) {
        User user = findById(id);
        user.setState(UserState.INACTIVO);
        userRepository.save(user);
        keycloakAdmin.disableUser(user.getUsername());
        eventPublisher.publishUserRegistered(user);
    }
}
