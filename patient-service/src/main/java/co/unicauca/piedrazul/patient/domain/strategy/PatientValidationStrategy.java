package co.unicauca.piedrazul.patient.domain.strategy;

import co.unicauca.piedrazul.patient.domain.entities.Patient;

/**
 * Interfaz del patrón Strategy para validación de pacientes.
 * Permite intercambiar reglas de validación según el contexto
 * de registro (web por el paciente vs manual por el agendador).
 *
 * @author Santiago Solarte
 */
public interface PatientValidationStrategy {

    /**
     * Valida los datos de un paciente según las reglas del contexto.
     *
     * @param patient Paciente a validar.
     * @throws IllegalArgumentException si algún campo no cumple las reglas.
     */
    void validate(Patient patient);
}
