package co.unicauca.piedrazul.identity;

import co.unicauca.piedrazul.identity.application.UserEventPublisher;
import co.unicauca.piedrazul.identity.domain.entities.Role;
import co.unicauca.piedrazul.identity.domain.entities.User;
import co.unicauca.piedrazul.identity.domain.enums.UserState;
import co.unicauca.piedrazul.identity.domain.repository.RoleRepository;
import co.unicauca.piedrazul.identity.domain.repository.UserRepository;
import co.unicauca.piedrazul.identity.domain.service.IdentityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para IdentityService.
 *
 * @author Santiago Solarte
 */
@ExtendWith(MockitoExtension.class)
class IdentityServiceTest {

    @Mock private UserRepository        userRepository;
    @Mock private RoleRepository        roleRepository;
    @Mock private BCryptPasswordEncoder passwordEncoder;
    @Mock private UserEventPublisher    eventPublisher;  // ← nuevo

    @InjectMocks
    private IdentityService identityService;

    private User testUser;
    private Role testRole;

    @BeforeEach
    void setUp() {
        testRole = new Role("PACIENTE");
        testRole.setRoleId(3);

        testUser = new User();
        testUser.setId(12345678);
        testUser.setUsername("test@correo.com");
        testUser.setPassword("hashedPassword");
        testUser.setFirstName("Juan");
        testUser.setFirstSurname("Pérez");
        testUser.setState(UserState.ACTIVO);
        testUser.setRoles(List.of(testRole));
    }

    // --- Tests de login ---

    @Test
    void login_credencialesCorrectas_retornaUsuario() {
        when(userRepository.findByUsername("test@correo.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", "hashedPassword")).thenReturn(true);

        User result = identityService.login("test@correo.com", "password123");

        assertNotNull(result);
        assertEquals("test@correo.com", result.getUsername());
    }

    @Test
    void login_usuarioNoExiste_lanzaExcepcion() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> identityService.login("noexiste@correo.com", "pass"));
    }

    @Test
    void login_contrasenaIncorrecta_lanzaExcepcion() {
        when(userRepository.findByUsername("test@correo.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongpass", "hashedPassword")).thenReturn(false);

        assertThrows(IllegalArgumentException.class,
                () -> identityService.login("test@correo.com", "wrongpass"));
    }

    @Test
    void login_usuarioInactivo_lanzaExcepcion() {
        testUser.setState(UserState.INACTIVO);
        when(userRepository.findByUsername("test@correo.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> identityService.login("test@correo.com", "password123"));
    }

    // --- Tests de registro ---

    @Test
    void register_usuarioNuevo_guardaCorrectamente() {
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(roleRepository.findByRoleName("PACIENTE")).thenReturn(Optional.of(testRole));
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User result = identityService.register(testUser, "PACIENTE");

        assertNotNull(result);
        verify(userRepository, times(1)).save(any(User.class));
        verify(eventPublisher, times(1)).publishUserRegistered(any(User.class));
    }

    @Test
    void register_usernameYaExiste_lanzaExcepcion() {
        when(userRepository.existsByUsername("test@correo.com")).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> identityService.register(testUser, "PACIENTE"));
    }

    @Test
    void register_rolNoExiste_lanzaExcepcion() {
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        when(roleRepository.findByRoleName("ROL_INVALIDO")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> identityService.register(testUser, "ROL_INVALIDO"));
    }

    // --- Tests de consulta ---

    @Test
    void findById_idExistente_retornaUsuario() {
        when(userRepository.findById(12345678L)).thenReturn(Optional.of(testUser));

        User result = identityService.findById(12345678);

        assertNotNull(result);
        assertEquals(12345678, result.getId());
    }

    @Test
    void findById_idNoExistente_lanzaExcepcion() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> identityService.findById(99999));
    }

    @Test
    void deactivate_usuarioActivo_cambiaEstado() {
        when(userRepository.findById(12345678L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        identityService.deactivate(12345678);

        assertEquals(UserState.INACTIVO, testUser.getState());
        verify(userRepository, times(1)).save(testUser);
        verify(eventPublisher, times(1)).publishUserRegistered(any(User.class));
    }
}