package co.unicauca.piedrazul.medical.domain.entities;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

import org.springframework.data.annotation.Id;


@RedisHash("occupied_slots")
public class OccupiedSlotCache implements Serializable {
    
    @Id
    private String id;

    @Indexed
    private int appointmentId;

    @Indexed
    private int doctorId;

    @Indexed
    private LocalDate date;

    private LocalTime startTime;
    private LocalTime endTime;
    private String status;

    public OccupiedSlotCache(){}
    
    // Getters y Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(int appointmentId) {
        this.appointmentId = appointmentId;
    }

    public int getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(int doctorId) {
        this.doctorId = doctorId;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
        
}
