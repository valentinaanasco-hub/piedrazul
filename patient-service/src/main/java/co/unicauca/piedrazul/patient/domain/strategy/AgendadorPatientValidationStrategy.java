package co.unicauca.piedrazul.patient.domain.strategy;

import co.unicauca.piedrazul.patient.domain.entities.Patient;
import org.springframework.stereotype.Component;

/**
 * Estrategia de validación para registro por agendador (RF2).
 * El agendador registra al paciente — fecha de nacimiento y correo son opcionales.
 * Contexto: patrón Strategy — validación flexible.
 *
 * @author Santiago Solarte
 */
@Component("agendadorValidation")
public class AgendadorPatientValidationStrategy implements PatientValidationStrategy {

    @Override
    public void validate(Patient patient) {
        validateRequired(patient.getFirstName(), "El primer nombre es obligatorio");
        validateRequired(patient.getFirstSurname(), "El apellido es obligatorio");
        validateRequired(patient.getPhone(), "El teléfono es obligatorio");
        validateRequired(patient.getGender(), "El género es obligatorio");
        validateRequired(patient.getUserTypeId(), "El tipo de documento es obligatorio");

        if (!patient.getPhone().matches("\\d{7,10}")) {
            throw new IllegalArgumentException("El teléfono debe tener entre 7 y 10 dígitos");
        }

        if (!patient.getGender().matches("Hombre|Mujer|Otro")) {
            throw new IllegalArgumentException("El género debe ser Hombre, Mujer u Otro");
        }

        if (patient.getId() <= 0) {
            throw new IllegalArgumentException("El número de documento debe ser mayor a 0");
        }

        // Correo opcional — solo validar formato si viene
        if (patient.getEmail() != null && !patient.getEmail().isBlank()) {
            if (!patient.getEmail().matches("^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$")) {
                throw new IllegalArgumentException("El formato del correo electrónico es inválido");
            }
        }
    }

    private void validateRequired(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
    }
}
