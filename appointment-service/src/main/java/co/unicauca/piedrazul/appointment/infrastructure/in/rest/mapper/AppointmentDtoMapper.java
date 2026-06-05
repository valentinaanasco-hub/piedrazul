package co.unicauca.piedrazul.appointment.infrastructure.in.rest.mapper;

import co.unicauca.piedrazul.appointment.domain.model.Appointment;
import co.unicauca.piedrazul.appointment.domain.service.builder.AppointmentDirector;
import co.unicauca.piedrazul.appointment.domain.service.builder.IAppointmentBuilder;
import co.unicauca.piedrazul.appointment.infrastructure.in.rest.dto.AppointmentDTOs.AppointmentResponse;
import co.unicauca.piedrazul.appointment.infrastructure.in.rest.dto.AppointmentDTOs.CreateAppointmentRequest;
import org.springframework.stereotype.Component;

/**
 * Mapper entre DTOs de infraestructura y el modelo de dominio
 * Usa el patrón Builder (Director + Builder) para construir la entidad de dominio
 */
@Component
public class AppointmentDtoMapper {

    private final AppointmentDirector  director;
    private final IAppointmentBuilder  builder;

    public AppointmentDtoMapper(AppointmentDirector director, IAppointmentBuilder builder) {
        this.director = director;
        this.builder  = builder;
    }

    public Appointment toEntity(CreateAppointmentRequest request) {
        director.buildManualAppointment(
                builder,
                request.doctorId(),
                request.patientId(),
                request.date(),
                request.startTime(),
                request.endTime(),
                request.reason(),
                request.notes()
        );
        return builder.build();
    }

    public AppointmentResponse toResponse(Appointment appointment) {
        return new AppointmentResponse(
                appointment.getAppointmentId(),
                appointment.getDoctorId(),
                appointment.getPatientId(),
                appointment.getDate(),
                appointment.getStartTime(),
                appointment.getEndTime(),
                appointment.getStatus(),
                appointment.getReason(),
                appointment.getNotes()
        );
    }
}
