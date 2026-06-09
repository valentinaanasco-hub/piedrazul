package co.unicauca.piedrazul.appointment.domain.model;

import co.unicauca.piedrazul.appointment.domain.exception.AppointmentConflictException;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Agregado raíz del dominio — representa una cita médica.
 * Modelo rico: contiene las transiciones de estado y sus invariantes de negocio.
 * Sin dependencias de frameworks.
 */
public class Appointment {

    private int appointmentId;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private AppointmentStatus status;
    private long doctorId;
    private String doctorName;
    private long patientId;
    private String reason;
    private String notes;
    private ServiceType serviceType;

    public Appointment() {
        this.status = AppointmentStatus.AGENDADA;
        this.reason = "Sin especificar";
    }

    // -------------------------------------------------------------------------
    // Comportamiento de dominio (transiciones de estado con invariantes)
    // -------------------------------------------------------------------------

    /**
     * Marca la cita como agendada por primera vez.
     */
    public void schedule() {
        this.status = AppointmentStatus.AGENDADA;
    }

    /**
     * Marca la cita como reagendada.
     * Solo se puede reagendar el mismo día en que está programada la cita.
     */
    public void markRescheduled() {
        this.status = AppointmentStatus.REAGENDADA;
    }

    /**
     * Cancela la cita.
     * Invariante: no se puede cancelar si ya está cancelada o atendida.
     */
    public void cancel() {
        if (this.status == AppointmentStatus.CANCELADA) {
            throw new AppointmentConflictException("La cita ya está cancelada");
        }
        if (this.status == AppointmentStatus.ATENDIDA) {
            throw new AppointmentConflictException("No se puede cancelar una cita que ya fue atendida");
        }
        this.status = AppointmentStatus.CANCELADA;
    }

    /**
     * Marca la cita como atendida.
     * Invariante: solo se puede atender si está agendada o reagendada, y es el día de la cita.
     */
    public void markAsAttended() {
        if (this.status == AppointmentStatus.CANCELADA) {
            throw new AppointmentConflictException("No se puede atender una cita cancelada");
        }
        if (this.status == AppointmentStatus.ATENDIDA) {
            throw new AppointmentConflictException("La cita ya fue atendida");
        }
        if (!this.date.equals(LocalDate.now())) {
            throw new AppointmentConflictException(
                "Solo se puede marcar como atendida el mismo día de la cita");
        }
        this.status = AppointmentStatus.ATENDIDA;
    }

    // -------------------------------------------------------------------------
    // Accesores (getters / setters — setters expuestos para mappers de persistencia)
    // -------------------------------------------------------------------------

    public int getAppointmentId() { return appointmentId; }
    public void setAppointmentId(int appointmentId)   { this.appointmentId = appointmentId; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
    public AppointmentStatus getStatus() { return status; }
    public void setStatus(AppointmentStatus status)   { this.status = status; }
    public long getDoctorId() { return doctorId; }
    public void setDoctorId(long doctorId) { this.doctorId = doctorId; }
    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }
    public long getPatientId() { return patientId; }
    public void setPatientId(long patientId)           { this.patientId = patientId; }
    public String getReason() { return reason; }
    public void setReason(String reason)              { this.reason = reason; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public ServiceType getServiceType() { return serviceType; }
    public void setServiceType(ServiceType serviceType) { this.serviceType = serviceType; }
}
