/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package co.unicauca.piedrazul.domain.services;

import co.unicauca.piedrazul.domain.acces.IPatientRepository;
import co.unicauca.piedrazul.domain.entities.Patient;
import java.util.List;

/**
 * @author Valentina Añasco 
 * @author Camila Dorado
 * @author Felipe Gutierrez
 * @author Ginner Ortega
 * @author Santiago Solarte 
 */

public class PatientService {
    private final IPatientRepository patientRepository;

    public PatientService(IPatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    public boolean registerPatient(Patient patient) {
        // Valida campos mínimos requeridos para agendar una cita
        if (patient.getFirstName() == null || patient.getFirstName().trim().isEmpty())
            throw new IllegalArgumentException("El nombre del paciente es obligatorio");
        if (patient.getFirstSurname() == null || patient.getFirstSurname().trim().isEmpty())
            throw new IllegalArgumentException("El apellido del paciente es obligatorio");
        if (patient.getPhone() == null || patient.getPhone().trim().isEmpty())
            throw new IllegalArgumentException("El teléfono es obligatorio");
        if (patient.getGender() == null || patient.getGender().trim().isEmpty())
            throw new IllegalArgumentException("El género es obligatorio");

        return patientRepository.save(patient);
    }

    public Patient findPatient(int id) {
        Patient patient = patientRepository.findById(id);
        if (patient == null)
            throw new IllegalArgumentException("Paciente no encontrado");
        return patient;
    }

    public List<Patient> listPatients() {
        return patientRepository.findAll();
    }

    public boolean modifyPatient(Patient patient) {
        if (patientRepository.findById(patient.getId()) == null)
            throw new IllegalArgumentException("Paciente no encontrado");
        return patientRepository.update(patient);
    }

    public boolean desactivatePatient(int id) {
        if (patientRepository.findById(id) == null)
            throw new IllegalArgumentException("Paciente no encontrado");
        return patientRepository.desactivate(id);
    }
}
