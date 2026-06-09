package co.unicauca.piedrazul.appointment.domain.model;

import java.time.LocalDateTime;

/**
 * Modelo de dominio: autorización de servicio otorgada por un médico a un paciente
 * al marcar una cita como atendida.
 * Una autorización activa permite al paciente acceder a un tipo de servicio especializado
 * en su próxima cita. Caduca en 1 mes si no se usa.
 */
public class PatientAuthorization {

    private int authId;
    private long patientId;
    private ServiceType serviceType;
    private LocalDateTime authorizedAt;
    private LocalDateTime expiresAt;
    private boolean used;
    private long authorizedByDoctorId;
    private int appointmentId;

    public PatientAuthorization() {}

    public PatientAuthorization(long patientId, ServiceType serviceType,
                                 LocalDateTime authorizedAt, LocalDateTime expiresAt,
                                 long authorizedByDoctorId, int appointmentId) {
        this.patientId            = patientId;
        this.serviceType          = serviceType;
        this.authorizedAt         = authorizedAt;
        this.expiresAt            = expiresAt;
        this.used                 = false;
        this.authorizedByDoctorId = authorizedByDoctorId;
        this.appointmentId        = appointmentId;
    }

    public int getAuthId()                              { return authId; }
    public void setAuthId(int authId)                   { this.authId = authId; }
    public long getPatientId()                           { return patientId; }
    public void setPatientId(long patientId)             { this.patientId = patientId; }
    public ServiceType getServiceType()                 { return serviceType; }
    public void setServiceType(ServiceType serviceType) { this.serviceType = serviceType; }
    public LocalDateTime getAuthorizedAt()              { return authorizedAt; }
    public void setAuthorizedAt(LocalDateTime at)       { this.authorizedAt = at; }
    public LocalDateTime getExpiresAt()                 { return expiresAt; }
    public void setExpiresAt(LocalDateTime at)          { this.expiresAt = at; }
    public boolean isUsed()                             { return used; }
    public void setUsed(boolean used)                   { this.used = used; }
    public long getAuthorizedByDoctorId()                { return authorizedByDoctorId; }
    public void setAuthorizedByDoctorId(long id)         { this.authorizedByDoctorId = id; }
    public int getAppointmentId()                       { return appointmentId; }
    public void setAppointmentId(int id)                { this.appointmentId = id; }
}
