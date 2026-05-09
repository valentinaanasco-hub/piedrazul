package co.unicauca.piedrazul.medical.domain.entities;

import jakarta.persistence.*;
import java.time.LocalTime;

/**
 * Entidad que representa el horario de un médico.
 * sched_day_of_week es un entero: 1=Lunes ... 6=Sábado, 7=Domingo.
 *
 * @author Ginner Ortega
 */
@Entity
@Table(name = "doctor_schedules")
public class DoctorSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sched_id")
    private int id;

    @ManyToOne
    @JoinColumn(name = "sched_doctor_id", nullable = false)
    private Doctor doctor;

    /** Día de la semana: 1=Lunes, 2=Martes, 3=Miércoles, 4=Jueves, 5=Viernes, 6=Sábado, 7=Domingo */
    @Column(name = "sched_day_of_week", nullable = false)
    private int dayOfWeek;

    @Column(name = "sched_start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "sched_end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "sched_interval_minutes", nullable = false)
    private int intervalMinutes;

    public DoctorSchedule() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public Doctor getDoctor() { return doctor; }
    public void setDoctor(Doctor doctor) { this.doctor = doctor; }
    public int getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(int dayOfWeek) { this.dayOfWeek = dayOfWeek; }
    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
    public int getIntervalMinutes() { return intervalMinutes; }
    public void setIntervalMinutes(int intervalMinutes) { this.intervalMinutes = intervalMinutes; }

    /** Nombre del día para mostrar en la UI */
    public String getDayName() {
        return switch (dayOfWeek) {
            case 1 -> "Lunes";
            case 2 -> "Martes";
            case 3 -> "Miércoles";
            case 4 -> "Jueves";
            case 5 -> "Viernes";
            case 6 -> "Sábado";
            case 7 -> "Domingo";
            default -> "Desconocido";
        };
    }
}