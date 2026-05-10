package co.unicauca.piedrazul.patient.domain.strategy;

import co.unicauca.piedrazul.patient.domain.entities.Patient;
import org.springframework.stereotype.Component;

/**
 * Estrategia de validación para registro web (RF3).
 * El paciente se registra solo — todos los campos obligatorios deben estar presentes.
 * Contexto: patrón Strategy — validación estricta.
 *
 * @author Santiago Solarte
 */
@Component("webValidation")
public class WebPatientValidationStrategy implements PatientValidationStrategy {

    @Override
    public void validate(Patient patient) {
        validateRequired(patient.getFirstName(), "El primer nombre es obligatorio");
        validateRequired(patient.getFirstSurname(), "El apellido es obligatorio");
        validateRequired(patient.getPhone(), "El teléfono es obligatorio");
        validateRequired(patient.getGender(), "El género es obligatorio");
        validateRequired(patient.getEmail(), "El correo electrónico es obligatorio");
        validateRequired(patient.getUsername(), "El correo de acceso es obligatorio");
        validateRequired(patient.getPassword(), "La contraseña es obligatoria");
        validateRequired(patient.getUserTypeId(), "El tipo de documento es obligatorio");

        if (!patient.getPhone().matches("\\d{7,10}")) {
            throw new IllegalArgumentException("El teléfono debe tener entre 7 y 10 dígitos");
        }

        if (!patient.getEmail().matches("^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$")) {
            throw new IllegalArgumentException("El formato del correo electrónico es inválido");
        }

        if (!patient.getGender().matches("Hombre|Mujer|Otro")) {
            throw new IllegalArgumentException("El género debe ser Hombre, Mujer u Otro");
        }

        if (patient.getId() <= 0) {
            throw new IllegalArgumentException("El número de documento debe ser mayor a 0");
        }
    }

    private void validateRequired(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
    }
}
