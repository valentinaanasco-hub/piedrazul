package co.unicauca.piedrazul.presentation.controllers;

import co.unicauca.piedrazul.domain.entities.DoctorSchedule;
import co.unicauca.piedrazul.domain.services.interfaces.IDoctorScheduleService;
import java.util.ArrayList;
import java.util.List;

public class DoctorScheduleController {
    private final IDoctorScheduleService scheduleService;
    private String lastErrorMessage;

    public DoctorScheduleController(IDoctorScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    // Registra una nueva franja horaria para un médico específico
    public boolean createSchedule(DoctorSchedule schedule, int doctorId) {
        try {
            lastErrorMessage = null;
            return scheduleService.registerSchedule(schedule, doctorId);
        } catch (IllegalArgumentException e) {
            lastErrorMessage = e.getMessage();
            return false;
        }
    }

    // Obtiene todos los horarios configurados para un médico
    public List<DoctorSchedule> getSchedulesByDoctor(int doctorId) {
        try {
            lastErrorMessage = null;
            return scheduleService.listSchedulesByDoctor(doctorId);
        } catch (Exception e) {
            lastErrorMessage = "Error al listar horarios: " + e.getMessage();
            return new ArrayList<>();
        }
    }

    // Actualiza la información de un horario ya existente
    public boolean updateSchedule(DoctorSchedule schedule) {
        try {
            lastErrorMessage = null;
            return scheduleService.modifySchedule(schedule);
        } catch (IllegalArgumentException e) {
            lastErrorMessage = e.getMessage();
            return false;
        }
    }

    // Elimina una franja horaria del sistema
    public boolean deleteSchedule(int scheduleId) {
        try {
            lastErrorMessage = null;
            return scheduleService.removeSchedule(scheduleId);
        } catch (IllegalArgumentException e) {
            lastErrorMessage = e.getMessage();
            return false;
        }
    }

    public String getLastErrorMessage() {
        return lastErrorMessage;
    }
}