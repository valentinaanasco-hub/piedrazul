package co.unicauca.piedrazul.presentation.controllers;

import co.unicauca.piedrazul.domain.entities.Doctor;
import co.unicauca.piedrazul.domain.services.interfaces.IDoctorService;
import java.util.ArrayList;
import java.util.List;

public class DoctorController {

    private final IDoctorService doctorService;
    private String lastErrorMessage;

    public DoctorController(IDoctorService doctorService) {
        this.doctorService = doctorService;
    }

    // Registra un nuevo médico validando especialidades y datos básicos
    public boolean enrollDoctor(Doctor doctor) {
        try {
            lastErrorMessage = null;
            return doctorService.registerDoctor(doctor);
        } catch (IllegalArgumentException e) {
            lastErrorMessage = e.getMessage();
            return false;
        }
    }

    // Busca un médico por su identificación única
    public Doctor getDoctorById(int id) {
        try {
            lastErrorMessage = null;
            return doctorService.findDoctor(id);
        } catch (IllegalArgumentException e) {
            lastErrorMessage = e.getMessage();
            return null;
        }
    }

    // Recupera únicamente los médicos que están habilitados para atender
    public List<Doctor> getActiveStaff() {
        try {
            lastErrorMessage = null;
            return doctorService.listActiveDoctors();
        } catch (Exception e) {
            lastErrorMessage = e.getMessage();
            return new ArrayList<>();
        }
    }

    // Actualiza la información profesional o personal del médico
    public boolean updateDoctorInfo(Doctor doctor) {
        try {
            lastErrorMessage = null;
            return doctorService.modifyDoctor(doctor);
        } catch (IllegalArgumentException e) {
            lastErrorMessage = e.getMessage();
            return false;
        }
    }

    // Realiza un borrado lógico del médico en el sistema
    public boolean disableDoctor(int id) {
        try {
            lastErrorMessage = null;
            return doctorService.deactivateDoctor(id);
        } catch (IllegalArgumentException e) {
            lastErrorMessage = e.getMessage();
            return false;
        }
    }

    public String getLastErrorMessage() {
        return lastErrorMessage;
    }
}