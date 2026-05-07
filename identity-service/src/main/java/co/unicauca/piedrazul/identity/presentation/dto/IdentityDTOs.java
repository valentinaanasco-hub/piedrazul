package co.unicauca.piedrazul.identity.presentation.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTOs del identity service.
 * Clases de transferencia de datos entre el cliente y la API.
 *
 * @author Santiago Solarte
 */
public class IdentityDTOs {

    // --- Request: Login ---
    public record LoginRequest(
            @NotBlank(message = "El usuario es obligatorio")
            String username,

            @NotBlank(message = "La contraseña es obligatoria")
            String password
    ) {}

    // --- Request: Registro de usuario ---
    public record RegisterUserRequest(
            @NotBlank(message = "El primer nombre es obligatorio")
            String firstName,

            String middleName,

            @NotBlank(message = "El apellido es obligatorio")
            String firstSurname,

            String lastName,

            @NotBlank(message = "El correo es obligatorio")
            String username,

            @NotBlank(message = "La contraseña es obligatoria")
            String password,

            @NotBlank(message = "El tipo de documento es obligatorio")
            String userTypeId,

            int userId,

            @NotBlank(message = "El rol es obligatorio")
            String roleName
    ) {}

    // --- Response: Usuario autenticado ---
    public record UserResponse(
            int id,
            String username,
            String fullName,
            String state,
            java.util.List<String> roles
    ) {}

    // --- Response: Mensaje genérico ---
    public record MessageResponse(String message) {}
}
