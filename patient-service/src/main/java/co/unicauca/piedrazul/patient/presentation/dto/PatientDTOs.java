package co.unicauca.piedrazul.patient.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * DTOs del patient-service.
 *
 * @author Santiago Solarte
 */
public class PatientDTOs {

    // --- Request: Registro web (RF3) — todos los campos obligatorios ---
    public record WebRegisterRequest(
            @Positive(message = "El número de documento debe ser positivo")
            int documentId,

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

    // --- Request: Registro por agendador (RF2) — fecha y correo opcionales ---
    public record AgendadorRegisterRequest(
            @Positive(message = "El número de documento debe ser positivo")
            int documentId,

            @NotBlank(message = "El tipo de documento es obligatorio")
            String userTypeId,

            @NotBlank(message = "El primer nombre es obligatorio")
            String firstName,

            String middleName,

            @NotBlank(message = "El apellido es obligatorio")
            String firstSurname,

            String lastName,

            String email,        // opcional

            @NotBlank(message = "El teléfono es obligatorio")
            String phone,

            @NotBlank(message = "El género es obligatorio")
            String gender,

            String birthDay,     // opcional
            String birthMonth,   // opcional
            String birthYear     // opcional
    ) {}

    // --- Response: Datos del paciente ---
    public record PatientResponse(
            int id,
            String fullName,
            String email,
            String phone,
            String gender,
            String birthDate,
            String userTypeId,
            String state
    ) {}

    // --- Response: Mensaje genérico ---
    public record MessageResponse(String message) {}
}
