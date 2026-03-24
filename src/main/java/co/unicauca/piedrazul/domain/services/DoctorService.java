/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package co.unicauca.piedrazul.domain.services;

import co.unicauca.piedrazul.domain.acces.IDoctorRepository;
import co.unicauca.piedrazul.domain.entities.Doctor;
import java.util.List;

/**
 * @author Valentina Añasco 
 * @author Camila Dorado
 * @author Felipe Gutierrez
 * @author Ginner Ortega
 * @author Santiago Solarte 
 */

public class DoctorService {
    
    private final IDoctorRepository doctorRepository;

    public DoctorService(IDoctorRepository doctorRepository) {
        this.doctorRepository = doctorRepository;
    }

    public boolean registerDoctor(Doctor doctor) {
        // Valida campos obligatorios del médico
        if (doctor.getFirstName() == null || doctor.getFirstName().trim().isEmpty())
            throw new IllegalArgumentException("El nombre del médico es obligatorio");
        if (doctor.getProfessionalId() == null || doctor.getProfessionalId().trim().isEmpty())
            throw new IllegalArgumentException("El id profesional es obligatorio");
        if (doctor.getUsername() == null || doctor.getUsername().trim().isEmpty())
            throw new IllegalArgumentException("El username es obligatorio");

        return doctorRepository.save(doctor);
    }

    public Doctor findDoctor(int id) {
        Doctor doctor = doctorRepository.findById(id);
        if (doctor == null)
            throw new IllegalArgumentException("Médico no encontrado");
        return doctor;
    }

    public List<Doctor> listActiveDoctors() {
        // Solo retorna médicos activos para el agendamiento
        return doctorRepository.findAllActive();
    }

    public boolean modifyDoctor(Doctor doctor) {
        if (doctorRepository.findById(doctor.getId()) == null)
            throw new IllegalArgumentException("Médico no encontrado");
        return doctorRepository.update(doctor);
    }

    public boolean deactivateDoctor(int id) {
        // Desactiva en lugar de eliminar para conservar historial de citas
        if (doctorRepository.findById(id) == null)
            throw new IllegalArgumentException("Médico no encontrado");
        return doctorRepository.desactivate(id);
    }
}
