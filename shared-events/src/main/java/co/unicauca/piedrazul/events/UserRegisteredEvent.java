package co.unicauca.piedrazul.events;

/**
 * Evento publicado por identity-service cuando un usuario es registrado o actualizado.
 * Consumido por appointment-service para mantener su caché local de usuarios.
 *
 * @param userId   ID del usuario (número de documento)
 * @param fullName Nombre completo del usuario
 * @param role     Rol del usuario: ADMIN, DOCTOR, PACIENTE, AGENDADOR
 * @param state    Estado del usuario: ACTIVO, INACTIVO
 */
public record UserRegisteredEvent(
        int userId,
        String fullName,
        String role,
        String state
) {}
