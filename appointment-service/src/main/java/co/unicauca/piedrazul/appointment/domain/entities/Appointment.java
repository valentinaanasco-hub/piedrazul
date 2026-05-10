package co.unicauca.piedrazul.appointment.domain.entities;

import co.unicauca.piedrazul.appointment.domain.entities.enums.AppointmentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Entidad que representa una cita médica
 * Mapeada a la tabla appointments de la base de datos
 */
@Entity
@Table(name = "appointments")
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "appt_id")
    private int appointmentId;

    @Column(name = "appt_date", nullable = false)
    private LocalDate date;

    @Column(name = "appt_start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "appt_end_time", nullable = false)
    private LocalTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "appt_status", length = 20)
    private AppointmentStatus status = AppointmentStatus.AGENDADA;

    @Column(name = "appt_doct_id", nullable = false)
    private int doctorId;

    @Column(name = "appt_pat_id", nullable = false)
    private int patientId;

    @Column(name = "appt_reason", nullable = false, length = 255)
    private String reason = "Sin especificar";

    @Column(name = "appt_notes", length = 500)
    private String notes;

    public Appointment() {}

    public int getAppointmentId() { return appointmentId; }
    public void setAppointmentId(int appointmentId) { this.appointmentId = appointmentId; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
    public AppointmentStatus getStatus() { return status; }
    public void setStatus(AppointmentStatus status) { this.status = status; }
    public int getDoctorId() { return doctorId; }
    public void setDoctorId(int doctorId) { this.doctorId = doctorId; }
    public int getPatientId() { return patientId; }
    public void setPatientId(int patientId) { this.patientId = patientId; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
