package co.unicauca.piedrazul.appointment.infrastructure.in.rest.mapper;

import co.unicauca.piedrazul.appointment.domain.model.Appointment;
import co.unicauca.piedrazul.appointment.domain.service.builder.AppointmentDirector;
import co.unicauca.piedrazul.appointment.infrastructure.in.rest.dto.AppointmentDTOs.AppointmentResponse;
import co.unicauca.piedrazul.appointment.infrastructure.in.rest.dto.AppointmentDTOs.CreateAppointmentRequest;
import org.springframework.stereotype.Component;

/**
 * Adaptador de entrada: convierte DTOs de infraestructura al modelo de dominio.
 *
 * Usa el patrón Builder+Director para construir objetos Appointment:
 *  - AppointmentDirector (inyectado, stateless) orquesta el orden de los pasos.
 *  - AppointmentBuilder  (instanciado por llamada dentro del Director) acumula los valores.
 *
 * El Director crea un AppointmentBuilder nuevo por cada invocación,
 * eliminando el estado compartido entre peticiones concurrentes.
 */
@Component
public class AppointmentDtoMapper {

    private final AppointmentDirector director;

    public AppointmentDtoMapper(AppointmentDirector director) {
        this.director = director;
    }

    public Appointment toEntity(CreateAppointmentRequest request) {
        return director.buildManualAppointment(
                request.doctorId(),
                request.doctorName(),
                request.patientId(),
                request.date(),
                request.startTime(),
                request.endTime(),
                request.reason(),
                request.notes(),
                request.serviceType()
        );
    }

    public AppointmentResponse toResponse(Appointment appointment) {
        return new AppointmentResponse(
                appointment.getAppointmentId(),
                appointment.getDoctorId(),
                appointment.getDoctorName(),
                appointment.getPatientId(),
                appointment.getDate(),
                appointment.getStartTime(),
                appointment.getEndTime(),
                appointment.getStatus(),
                appointment.getReason(),
                appointment.getNotes(),
                appointment.getServiceType()
        );
    }
}
