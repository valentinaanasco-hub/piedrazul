package co.unicauca.piedrazul.presentation.controllers;

import co.unicauca.piedrazul.domain.entities.Appointment;
import co.unicauca.piedrazul.domain.services.interfaces.IAppointmentService;
import java.util.ArrayList;
import java.util.List;

public class ManualAppointmentController {

    private final IAppointmentService appointmentService;
    private String lastErrorMessage;

    public ManualAppointmentController(IAppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    // 1. Agendar Cita
    public boolean schedule(Appointment appointment) {
        try {
            lastErrorMessage = null;
            return appointmentService.scheduleAppointment(appointment);
        } catch (IllegalArgumentException e) {
            lastErrorMessage = e.getMessage();
            return false;
        }
    }

    // 2. Reagendar Cita
    public boolean reschedule(Appointment appointment) {
        try {
            lastErrorMessage = null;
            return appointmentService.rescheduleAppointment(appointment);
        } catch (IllegalArgumentException e) {
            lastErrorMessage = e.getMessage();
            return false;
        }
    }

    // 3. Cancelar Cita
    public boolean cancel(int id) {
        try {
            lastErrorMessage = null;
            return appointmentService.cancelAppointment(id);
        } catch (IllegalArgumentException e) {
            lastErrorMessage = e.getMessage();
            return false;
        }
    }

    // 4. Marcar como Atendida
    public boolean markAsAttended(int id) {
        try {
            lastErrorMessage = null;
            return appointmentService.markAsAttended(id);
        } catch (IllegalArgumentException e) {
            lastErrorMessage = e.getMessage();
            return false;
        }
    }

    // 5. Buscar una cita específica
    public Appointment find(int id) {
        try {
            lastErrorMessage = null;
            return appointmentService.findAppointment(id);
        } catch (IllegalArgumentException e) {
            lastErrorMessage = e.getMessage();
            return null;
        }
    }

    // 6. Listar todas las citas
    public List<Appointment> list() {
        try {
            lastErrorMessage = null;
            return appointmentService.listAppointments();
        } catch (Exception e) {
            // En listados, si algo falla devolvemos lista vacía
            lastErrorMessage = "Error al cargar la lista: " + e.getMessage();
            return new ArrayList<>();
        }
    }

    // Método para que la vista consulte qué salió mal
    public String getLastErrorMessage() {
        return lastErrorMessage;
    }
}