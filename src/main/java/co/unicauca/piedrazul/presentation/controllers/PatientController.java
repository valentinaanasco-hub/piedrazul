package co.unicauca.piedrazul.presentation.controllers;

import java.util.List;
import co.unicauca.piedrazul.domain.entities.Patient;
import co.unicauca.piedrazul.domain.services.interfaces.IPatientService;
import java.util.ArrayList;

/**
 * @author Valentina Añasco 
 * @author Camila Dorado
 * @author Felipe Gutierrez
 * @author Ginner Ortega
 * @author Santiago Solarte 
 */



public class PatientController {

    private final IPatientService patientService;
    private String lastErrorMessage;

    public PatientController(IPatientService patientService) {
        this.patientService = patientService;
    }

    // Busca un paciente por su ID (usado en el botón "Buscar" de tu vista)
    public Patient getPatientById(int id) {
        try {
            lastErrorMessage = null;
            return patientService.findPatient(id);
        } catch (IllegalArgumentException e) {
            lastErrorMessage = e.getMessage();
            return null;
        }
    }

    // Registra un nuevo paciente
    public boolean enrollPatient(Patient patient) {
        try {
            lastErrorMessage = null;
            return patientService.registerPatient(patient);
        } catch (IllegalArgumentException e) {
            lastErrorMessage = e.getMessage();
            return false;
        }
    }

    // Lista todos los pacientes registrados
    public List<Patient> getAllPatients() {
        try {
            lastErrorMessage = null;
            return patientService.listPatients();
        } catch (Exception e) {
            lastErrorMessage = e.getMessage();
            return new ArrayList<>();
        }
    }

    // Actualiza datos del paciente
    public boolean updatePatientInfo(Patient patient) {
        try {
            lastErrorMessage = null;
            return patientService.modifyPatient(patient);
        } catch (IllegalArgumentException e) {
            lastErrorMessage = e.getMessage();
            return false;
        }
    }

    public String getLastErrorMessage() {
        return lastErrorMessage;
    }
}
