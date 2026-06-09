package co.unicauca.piedrazul.appointment.domain.service.builder;

import co.unicauca.piedrazul.appointment.domain.model.Appointment;
import co.unicauca.piedrazul.appointment.domain.model.ServiceType;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Implementación concreta del Builder para Appointment.
 * Acumula los valores campo por campo mediante una API fluente.
 * No es un bean de Spring: debe instanciarse con new por cada uso.
 */
public class AppointmentBuilder implements IAppointmentBuilder {

    private long doctorId;
    private String doctorName;
    private long patientId;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private String reason = "Sin especificar";
    private String notes;
    private ServiceType serviceType = ServiceType.CONSULTA_GENERAL;

    @Override
    public IAppointmentBuilder doctorId(long doctorId) { this.doctorId = doctorId; return this; }
    @Override
    public IAppointmentBuilder doctorName(String doctorName) { this.doctorName = doctorName; return this; }
    @Override
    public IAppointmentBuilder patientId(long patientId) { this.patientId = patientId; return this; }
    @Override
    public IAppointmentBuilder date(LocalDate date) { this.date = date; return this; }
    @Override
    public IAppointmentBuilder startTime(LocalTime t) { this.startTime = t; return this; }
    @Override
    public IAppointmentBuilder endTime(LocalTime t) { this.endTime = t; return this; }
    @Override
    public IAppointmentBuilder reason(String reason) { this.reason = reason; return this; }
    @Override
    public IAppointmentBuilder notes(String notes) { this.notes = notes; return this; }
    @Override
    public IAppointmentBuilder serviceType(ServiceType serviceType) { this.serviceType = serviceType; return this; }

    @Override
    public Appointment build() {
        Appointment appointment = new Appointment();
        appointment.setDoctorId(doctorId);
        appointment.setDoctorName(doctorName);
        appointment.setPatientId(patientId);
        appointment.setDate(date);
        appointment.setStartTime(startTime);
        appointment.setEndTime(endTime);
        appointment.setReason(reason);
        appointment.setNotes(notes);
        appointment.setServiceType(serviceType);
        return appointment;
    }
}
