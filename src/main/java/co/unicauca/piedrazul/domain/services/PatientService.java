package co.unicauca.piedrazul.domain.services;

import co.unicauca.piedrazul.domain.entities.Patient;
import java.util.List;
import co.unicauca.piedrazul.domain.access.IPatientRepository;
import co.unicauca.piedrazul.domain.services.interfaces.IPatientValidator;

/**
 * @author Valentina Añasco
 * @author Camila Dorado
 * @author Felipe Gutierrez
 * @author Ginner Ortega
 * @author Santiago Solarte
 */
public class PatientService {

    private final IPatientRepository patientRepository;
    private final IPatientValidator validator; // Inyectamos la interfaz del validador

    public PatientService(IPatientRepository patientRepository, IPatientValidator validator) {
        this.patientRepository = patientRepository;
        this.validator = validator;
    }

    public boolean registerPatient(Patient patient) {
        validator.validatePatient(patient);

        return patientRepository.save(patient);
    }

    public Patient findPatient(int id) {
        Patient patient = patientRepository.findById(id);

        validator.validateExists(patient);

        return patient;
    }

    public List<Patient> listPatients() {
        return patientRepository.findAll();
    }

    public boolean modifyPatient(Patient patient) {
        // Validamos que el paciente tenga datos correctos
        validator.validatePatient(patient);

        // Verificamos que exista antes de modificar
        Patient existing = patientRepository.findById(patient.getId());
        validator.validateExists(existing);

        return patientRepository.update(patient);
    }

    public boolean deactivatePatient(int id) {
        // Verificamos existencia
        Patient patient = patientRepository.findById(id);
        validator.validateExists(patient);

        return patientRepository.deactivate(id);
    }
}
