package co.unicauca.piedrazul.presentation.controllers;

import co.unicauca.piedrazul.domain.entities.Specialty;
import co.unicauca.piedrazul.domain.services.interfaces.ISpecialtyService;
import java.util.ArrayList;
import java.util.List;

public class SpecialtyController {
    private final ISpecialtyService specialtyService;
    private String lastErrorMessage;

    public SpecialtyController(ISpecialtyService specialtyService) {
        this.specialtyService = specialtyService;
    }

    // Busca una especialidad por su nombre exacto
    public Specialty getByName(String name) {
        try {
            lastErrorMessage = null;
            return specialtyService.findByName(name);
        } catch (IllegalArgumentException e) {
            lastErrorMessage = e.getMessage();
            return null;
        }
    }

    // Busca una especialidad por su identificador numérico
    public Specialty getById(int id) {
        try {
            lastErrorMessage = null;
            return specialtyService.findSpecialty(id);
        } catch (IllegalArgumentException e) {
            lastErrorMessage = e.getMessage();
            return null;
        }
    }

    // Retorna todas las especialidades disponibles en la clínica
    public List<Specialty> getAllSpecialties() {
        try {
            lastErrorMessage = null;
            return specialtyService.listSpecialties();
        } catch (Exception e) {
            lastErrorMessage = e.getMessage();
            return new ArrayList<>();
        }
    }

    // Vincula profesionalmente a un médico con una especialidad
    public boolean linkSpecialtyToDoctor(int doctorId, int specialtyId) {
        try {
            lastErrorMessage = null;
            return specialtyService.assignSpecialtyToDoctor(doctorId, specialtyId);
        } catch (IllegalArgumentException e) {
            lastErrorMessage = e.getMessage();
            return false;
        }
    }

    // Lista las especialidades que posee un médico en particular
    public List<Specialty> getDoctorSpecialties(int doctorId) {
        try {
            lastErrorMessage = null;
            return specialtyService.findByDoctorId(doctorId);
        } catch (Exception e) {
            lastErrorMessage = e.getMessage();
            return new ArrayList<>();
        }
    }

    public String getLastErrorMessage() {
        return lastErrorMessage;
    }
}