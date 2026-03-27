package co.unicauca.piedrazul.domain.services;

import co.unicauca.piedrazul.domain.access.IUserRepository;
import co.unicauca.piedrazul.domain.entities.User;
import co.unicauca.piedrazul.domain.entities.enums.UserState;
import co.unicauca.piedrazul.domain.services.interfaces.IUserValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para UserService.
 *
 * Cubre login (credenciales válidas, contraseña incorrecta, usuario inactivo,
 * usuario inexistente), registro y operaciones de gestión.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private IUserRepository userRepository;

    @Mock
    private IUserValidator validator;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, validator);
    }

    // -----------------------------------------------------------------------
    // Helper
    // -----------------------------------------------------------------------

    private User buildUser(String username, String password, UserState state) {
        User u = new User();
        u.setUsername(username);
        u.setPassword(password);
        u.setState(state);
        u.setFirstName("Juan");
        u.setFirstSurname("Perez");
        u.setRoles(new ArrayList<>()); // Evita NullPointerException en registerUser
        return u;
    }

    // -----------------------------------------------------------------------
    // login — Test 1: credenciales correctas → retorna usuario
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("login: credenciales válidas y usuario activo -> retorna el usuario")
    void login_credencialesCorrectas_retornaUsuario() {
        // GIVEN
        User user = buildUser("jperez", "pass123", UserState.ACTIVO);
        when(userRepository.findByUsername("jperez")).thenReturn(user);

        // WHEN
        User result = userService.login("jperez", "pass123");

        // THEN
        assertNotNull(result);
        assertEquals("jperez", result.getUsername());
        verify(validator).validateUserName("jperez");
        verify(validator).validatePassword("pass123");
        verify(validator).validateExists(user);
    }

    // -----------------------------------------------------------------------
    // login — Test 2: contraseña incorrecta → IllegalArgumentException
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("login: contraseña incorrecta -> lanza IllegalArgumentException")
    void login_passwordIncorrecta_lanzaExcepcion() {
        // GIVEN: el usuario existe con password "pass123"
        User user = buildUser("jperez", "pass123", UserState.ACTIVO);
        when(userRepository.findByUsername("jperez")).thenReturn(user);

        // WHEN / THEN: se intenta login con password distinto
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> userService.login("jperez", "wrongpass"));

        assertEquals("Contraseña incorrecta", ex.getMessage());
    }

    // -----------------------------------------------------------------------
    // login — Test 3: usuario INACTIVO → IllegalArgumentException
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("login: usuario inactivo -> lanza IllegalArgumentException")
    void login_usuarioInactivo_lanzaExcepcion() {
        // GIVEN: credenciales correctas pero usuario INACTIVO
        User user = buildUser("jperez", "pass123", UserState.INACTIVO);
        when(userRepository.findByUsername("jperez")).thenReturn(user);

        // WHEN / THEN
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> userService.login("jperez", "pass123"));

        assertEquals("Usuario inactivo", ex.getMessage());
    }

    // -----------------------------------------------------------------------
    // login — Test 4: usuario no existe → validator lanza excepción
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("login: usuario no existe en el sistema -> validator lanza excepción")
    void login_usuarioNoExiste_lanzaExcepcion() {
        // GIVEN: el repositorio no encuentra al usuario
        when(userRepository.findByUsername("fantasma")).thenReturn(null);
        doThrow(new IllegalArgumentException("Usuario no encontrado"))
                .when(validator).validateExists(null);

        // WHEN / THEN
        assertThrows(IllegalArgumentException.class,
                () -> userService.login("fantasma", "pass123"));
    }

    // -----------------------------------------------------------------------
    // registerUser — Test 5: usuario nuevo → guarda correctamente
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("registerUser: username no duplicado -> guarda y retorna true")
    void registerUser_usuarioNuevo_guardaYRetornaTrue() {
        // GIVEN: el username no existe en el repositorio
        User user = buildUser("nuevo", "pass123", UserState.ACTIVO);
        when(userRepository.findByUsername("nuevo")).thenReturn(null); // No existe → ok
        when(userRepository.save(user)).thenReturn(true);

        // WHEN
        boolean result = userService.registerUser(user);

        // THEN
        assertTrue(result);
        verify(validator).validateUser(user);
        verify(userRepository).save(user);
    }

    // -----------------------------------------------------------------------
    // registerUser — Test 6: username duplicado → IllegalArgumentException
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("registerUser: username ya registrado -> lanza IllegalArgumentException")
    void registerUser_usernameDuplicado_lanzaExcepcion() {
        // GIVEN: ya existe un usuario con ese username
        User existente = buildUser("duplicado", "otrapass", UserState.ACTIVO);
        User nuevo = buildUser("duplicado", "pass123", UserState.ACTIVO);

        // findByUsername retorna el existente → el servicio lanza excepción directamente
        when(userRepository.findByUsername("duplicado")).thenReturn(existente);

        // WHEN / THEN: el servicio detecta el duplicado y lanza la excepción él mismo
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> userService.registerUser(nuevo));

        assertTrue(ex.getMessage().contains("ya está registrado"));
        verify(userRepository, never()).save(any());
    }

    // -----------------------------------------------------------------------
    // listUsers — Test 7: retorna la lista completa
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("listUsers: retorna la lista completa del repositorio")
    void listUsers_retornaListaCompleta() {
        List<User> users = List.of(
                buildUser("u1", "p1", UserState.ACTIVO),
                buildUser("u2", "p2", UserState.INACTIVO)
        );
        when(userRepository.findAll()).thenReturn(users);

        List<User> result = userService.listUsers();

        assertEquals(2, result.size());
        verify(userRepository).findAll();
    }

    // -----------------------------------------------------------------------
    // deactivateUser — Test 8: desactivar usuario existente
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("deactivateUser: id válido -> delega al repositorio y retorna true")
    void deactivateUser_idValido_retornaTrue() {
        when(userRepository.deactivate(5)).thenReturn(true);

        boolean result = userService.deactivateUser(5);

        assertTrue(result);
        verify(userRepository).deactivate(5);
    }
}