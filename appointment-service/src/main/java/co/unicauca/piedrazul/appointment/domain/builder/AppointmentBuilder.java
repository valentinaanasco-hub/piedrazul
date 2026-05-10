package co.unicauca.piedrazul.appointment.domain.builder;

import co.unicauca.piedrazul.appointment.domain.entities.Appointment;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Implementación concreta del Builder para Appointment
 * Acumula los valores de cada paso y construye el objeto final con build()
 */
@Component
public class AppointmentBuilder implements IAppointmentBuilder {

    private int doctorId;
    private int patientId;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private String reason = "Sin especificar";
    private String notes;

    @Override
    public IAppointmentBuilder doctorId(int doctorId) {
        this.doctorId = doctorId;
        return this;
    }

    @Override
    public IAppointmentBuilder patientId(int patientId) {
        this.patientId = patientId;
        return this;
    }

    @Override
    public IAppointmentBuilder date(LocalDate date) {
        this.date = date;
        return this;
    }

    @Override
    public IAppointmentBuilder startTime(LocalTime startTime) {
        this.startTime = startTime;
        return this;
    }

    @Override
    public IAppointmentBuilder endTime(LocalTime endTime) {
        this.endTime = endTime;
        return this;
    }

    @Override
    public IAppointmentBuilder reason(String reason) {
        this.reason = reason;
        return this;
    }

    @Override
    public IAppointmentBuilder notes(String notes) {
        this.notes = notes;
        return this;
    }

    /**
     * Construye y retorna el objeto Appointment con los valores acumulados
     * El cliente obtiene el producto directamente del Builder, no del Director
     */
    @Override
    public Appointment build() {
        Appointment appointment = new Appointment();
        appointment.setDoctorId(doctorId);
        appointment.setPatientId(patientId);
        appointment.setDate(date);
        appointment.setStartTime(startTime);
        appointment.setEndTime(endTime);
        appointment.setReason(reason);
        appointment.setNotes(notes);
        return appointment;
    }
}
