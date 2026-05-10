package co.unicauca.piedrazul.appointment.domain.builder;

import co.unicauca.piedrazul.appointment.presentation.dto.AppointmentDTOs.CreateAppointmentRequest;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Director del patrón Builder para Appointment
 * Orquesta el orden de los pasos de construcción según el tipo de cita
 * No devuelve el producto — el cliente lo obtiene llamando builder.build()
 */
@Component
public class AppointmentDirector {

    /**
     * Configura el builder para una cita creada manualmente por el agendador
     * Incluye razón y notas opcionales del request
     *
     * @param builder el builder a configurar
     * @param request datos del request del agendador
     */
    public void buildManualAppointment(IAppointmentBuilder builder,
                                        CreateAppointmentRequest request) {
        String reason;
        if (request.reason() != null) {
            reason = request.reason();
        } else {
            reason = "Sin especificar";
        }

        builder.doctorId(request.doctorId());
        builder.patientId(request.patientId());
        builder.date(request.date());
        builder.startTime(request.startTime());
        builder.endTime(request.endTime());
        builder.reason(reason);
        builder.notes(request.notes());
    }

    /**
     * Configura el builder para una cita agendada por el paciente desde la web
     * La razón es fija y no incluye notas internas
     *
     * @param builder   el builder a configurar
     * @param doctorId  ID del médico seleccionado
     * @param patientId ID del paciente
     * @param date      fecha de la cita
     * @param startTime hora de inicio
     * @param endTime   hora de fin
     */
    public void buildWebAppointment(IAppointmentBuilder builder,
                                     int doctorId, int patientId,
                                     LocalDate date, LocalTime startTime, LocalTime endTime) {
        builder.doctorId(doctorId);
        builder.patientId(patientId);
        builder.date(date);
        builder.startTime(startTime);
        builder.endTime(endTime);
        builder.reason("Cita agendada por paciente");
    }
}
