package co.unicauca.piedrazul.appointment.presentation.mapper;

import co.unicauca.piedrazul.appointment.domain.entities.Appointment;
import co.unicauca.piedrazul.appointment.presentation.dto.AppointmentDTOs.AppointmentResponse;
import co.unicauca.piedrazul.appointment.presentation.dto.AppointmentDTOs.CreateAppointmentRequest;
import org.springframework.stereotype.Component;

/**
 * Mapper para convertir entre entidad Appointment y sus DTOs
 */
@Component
public class AppointmentMapper {

    public Appointment toEntity(CreateAppointmentRequest request) {
        Appointment appointment = new Appointment();
        appointment.setDoctorId(request.doctorId());
        appointment.setPatientId(request.patientId());
        appointment.setDate(request.date());
        appointment.setStartTime(request.startTime());
        appointment.setEndTime(request.endTime());
        if (request.reason() != null) {
            appointment.setReason(request.reason());
        } else {
            appointment.setReason("Sin especificar");
        }
        appointment.setNotes(request.notes());
        return appointment;
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
