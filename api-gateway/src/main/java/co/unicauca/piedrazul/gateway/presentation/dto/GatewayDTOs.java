package co.unicauca.piedrazul.gateway.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

/**
 * DTOs del API Gateway para orquestación de registro completo de paciente.
 *
 * @author Santiago Solarte
 */
public class GatewayDTOs {

    /**
     * Request unificado de registro web (RF3).
     * El gateway lo divide internamente entre identity-service y patient-service.
     */
    public record PatientRegisterRequest(
            @Positive(message = "El número de documento debe ser positivo")
            long documentId,

            @NotBlank(message = "El tipo de documento es obligatorio")
            String userTypeId,

            @NotBlank(message = "El primer nombre es obligatorio")
            String firstName,

            String middleName,

            @NotBlank(message = "El apellido es obligatorio")
            String firstSurname,

            String lastName,

            @NotBlank(message = "El correo es obligatorio")
            String email,

            @NotBlank(message = "La contraseña es obligatoria")
            String password,

            @NotBlank(message = "El teléfono es obligatorio")
            String phone,

            @NotBlank(message = "El género es obligatorio")
            String gender,

            String birthDay,
            String birthMonth,
            String birthYear
    ) {}

    // --- Request para identity-service ---
    public record IdentityRegisterRequest(
            String firstName,
            String middleName,
            String firstSurname,
            String lastName,
            String username,
            String password,
            String userTypeId,
            long userId,
            String roleName
    ) {}

    // --- Request para patient-service ---
    public record PatientServiceRequest(
            long documentId,
            String userTypeId,
            String firstName,
            String middleName,
            String firstSurname,
            String lastName,
            String email,
            String password,
            String phone,
            String gender,
            String birthDay,
            String birthMonth,
            String birthYear
    ) {}

    // --- Response genérico ---
    public record MessageResponse(String message) {}

    // --- Response de registro exitoso ---
    public record RegisterResponse(
            String message,
            long userId,
            String fullName,
            String email
    ) {}
}
