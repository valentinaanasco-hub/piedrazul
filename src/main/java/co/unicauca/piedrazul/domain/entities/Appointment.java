package co.unicauca.piedrazul.domain.entities;

import co.unicauca.piedrazul.domain.entities.enums.AppointmentStatus;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * @author Valentina Añasco
 * @author Camila Dorado
 * @author Felipe Gutierrez
 * @author Ginner Ortega
 * @author Santiago Solarte
 */
public class Appointment {

    private int appointmentId;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private AppointmentStatus status; // AGENDADA, CANCELADA, ATENDIDA
    private Doctor doctor;
    private Patient patient;

    // Constructor vacío (necesario para mapeo desde repositorio)
    public Appointment() {
    }
        public Appointment(int appointmentId, LocalDate date, LocalTime startTime, LocalTime endTime, AppointmentStatus status, Doctor doctor, Patient patient) {
        this.appointmentId = appointmentId;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.doctor = doctor;
        this.patient = patient;
    }
    public Appointment(LocalDate date, LocalTime startTime, LocalTime endTime, AppointmentStatus status, Doctor doctor, Patient patient) {
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.doctor = doctor;
        this.patient = patient;
    }



    public int getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(int appointmentId) {
        this.appointmentId = appointmentId;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public AppointmentStatus getStatus() {
        return status;
    }

    public void setStatus(AppointmentStatus status) {
        this.status = status;
    }

    public Doctor getDoctor() {
        return doctor;
    }

    public void setDoctor(Doctor doctor) {
        this.doctor = doctor;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }
}
