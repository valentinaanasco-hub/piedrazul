package co.unicauca.piedrazul.patient.domain.service;

import co.unicauca.piedrazul.patient.domain.entities.Patient;
import co.unicauca.piedrazul.patient.domain.repository.PatientRepository;
import co.unicauca.piedrazul.patient.domain.strategy.PatientValidationStrategy;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Servicio de dominio para gestión de pacientes.
 * Usa el patrón Strategy para intercambiar reglas de validación
 * según el contexto de registro (web vs agendador).
 *
 * @author Santiago Solarte
 */
@Service
public class PatientService {

    private final PatientRepository patientRepository;
    private final PatientValidationStrategy webValidation;
    private final PatientValidationStrategy agendadorValidation;

    public PatientService(
            PatientRepository patientRepository,
            @Qualifier("webValidation") PatientValidationStrategy webValidation,
            @Qualifier("agendadorValidation") PatientValidationStrategy agendadorValidation) {
        this.patientRepository = patientRepository;
        this.webValidation = webValidation;
        this.agendadorValidation = agendadorValidation;
    }

    /**
     * Registra un paciente desde la web (RF3).
     * Usa WebPatientValidationStrategy — todos los campos obligatorios.
     */
    @Transactional
    public Patient registerFromWeb(Patient patient) {
        webValidation.validate(patient);
        validateNotDuplicate(patient.getId());
        return patientRepository.save(patient);
    }

    /**
     * Registra un paciente desde el agendador (RF2).
     * Usa AgendadorPatientValidationStrategy — fecha y correo opcionales.
     */
    @Transactional
    public Patient registerFromAgendador(Patient patient) {
        agendadorValidation.validate(patient);
        validateNotDuplicate(patient.getId());
        return patientRepository.save(patient);
    }

    /**
     * Busca un paciente por su número de documento (id).
     */
    public Patient findById(int id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Paciente no encontrado"));
    }

    /**
     * Lista todos los pacientes del sistema.
     */
    public List<Patient> listAll() {
        return patientRepository.findAll();
    }

    /**
     * Actualiza los datos de un paciente existente.
     */
    @Transactional
    public Patient update(Patient patient) {
        findById(patient.getId());
        agendadorValidation.validate(patient);
        return patientRepository.save(patient);
    }

    /**
     * Desactiva un paciente sin eliminarlo.
     * La desactivación se delega al identity-service (campo user_state).
     * Aquí solo verificamos que el paciente existe.
     */
    public void validateExists(int id) {
        findById(id);
    }

    // --- Validación interna ---

    private void validateNotDuplicate(int id) {
        if (patientRepository.existsById(id)) {
            throw new IllegalArgumentException("Ya existe un paciente registrado con ese número de documento");
        }
    }
}
