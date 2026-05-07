package co.unicauca.piedrazul.presentation.controllers;

import co.unicauca.piedrazul.domain.services.interfaces.IAvailabilityService;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class AvailabilityController {

    private final IAvailabilityService availabilityService;
    private String lastErrorMessage;

    public AvailabilityController(IAvailabilityService availabilityService) {
        this.availabilityService = availabilityService;
    }

    // Calcula los huecos disponibles para un médico en una fecha específica
    public List<LocalTime> checkAvailableSlots(int doctorId, LocalDate date) {
        try {
            lastErrorMessage = null;
            return availabilityService.getAvailableSlots(doctorId, date);
        } catch (Exception e) {
            lastErrorMessage = "Error al calcular disponibilidad: " + e.getMessage();
            return new ArrayList<>();
        }
    }

    // Obtiene la duración estándar de las citas para un médico ese día (ej: 20 min)
    public int getDoctorTimeInterval(int doctorId, LocalDate date) {
        try {
            lastErrorMessage = null;
            return availabilityService.getIntervalMinutesForDoctorOnDate(doctorId, date);
        } catch (Exception e) {
            lastErrorMessage = e.getMessage();
            return 30; // Retorno preventivo de 30 minutos
        }
    }

    public String getLastErrorMessage() {
        return lastErrorMessage;
    }
}