package co.unicauca.piedrazul.appointment.presentation.mapper;

import co.unicauca.piedrazul.appointment.domain.builder.AppointmentDirector;
import co.unicauca.piedrazul.appointment.domain.builder.IAppointmentBuilder;
import co.unicauca.piedrazul.appointment.domain.entities.Appointment;
import co.unicauca.piedrazul.appointment.presentation.dto.AppointmentDTOs.AppointmentResponse;
import co.unicauca.piedrazul.appointment.presentation.dto.AppointmentDTOs.CreateAppointmentRequest;
import org.springframework.stereotype.Component;

/**
 * Mapper para convertir entre entidad Appointment y sus DTOs
 * Usa el patrón Builder (Director + Builder) para construir la entidad
 */
@Component
public class AppointmentMapper {

    private final AppointmentDirector director;
    private final IAppointmentBuilder builder;

    public AppointmentMapper(AppointmentDirector director, IAppointmentBuilder builder) {
        this.director = director;
        this.builder = builder;
    }

    /**
     * Convierte un CreateAppointmentRequest a entidad Appointment
     * El Director configura los pasos, el cliente obtiene el resultado del Builder
     */
    public Appointment toEntity(CreateAppointmentRequest request) {
        director.buildManualAppointment(builder, request);
        return builder.build();
    }

    /**
     * Convierte una entidad Appointment a AppointmentResponse
     */
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
