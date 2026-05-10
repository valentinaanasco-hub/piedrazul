package co.unicauca.piedrazul.medical.application;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import co.unicauca.piedrazul.medical.domain.entities.Doctor;
import co.unicauca.piedrazul.medical.domain.entities.DoctorSchedule;
import co.unicauca.piedrazul.medical.domain.service.MedicalStaffService;
import co.unicauca.piedrazul.medical.presentation.dto.MedicalDTOs.DoctorFullInfoResponse;
import co.unicauca.piedrazul.medical.presentation.dto.MedicalDTOs.DoctorResponse;
import co.unicauca.piedrazul.medical.presentation.dto.MedicalDTOs.ScheduleResponse;
import co.unicauca.piedrazul.medical.presentation.mapper.MedicalMapper;

/**
 * Facade del patrón Facade para el módulo de personal médico.
 * Simplifica la interfaz del subsistema combinando en una sola llamada:
 * datos del médico, sus horarios y las franjas disponibles para una fecha.
 * El cliente no necesita conocer ni coordinar los subsistemas internos.
 */
@Component
public class DoctorFacade {

    private final MedicalStaffService medicalStaffService;
    private final MedicalMapper medicalMapper;

    public DoctorFacade(MedicalStaffService medicalStaffService,
                        MedicalMapper medicalMapper) {
        this.medicalStaffService = medicalStaffService;
        this.medicalMapper = medicalMapper;
    }

    /**
     * Retorna en una sola llamada los datos del médico,
     * sus horarios configurados y las franjas disponibles para la fecha dada.
     * Oculta la complejidad de coordinar tres subsistemas distintos.
     *
     * @param doctorId      ID del médico
     * @param date          Fecha para calcular disponibilidad
     * @param occupiedSlots Franjas ya ocupadas por citas existentes
     * @return Respuesta completa con médico, horarios y slots disponibles
     */
    public DoctorFullInfoResponse getDoctorFullInfo(int doctorId,
                                                     LocalDate date,
                                                     List<String> occupiedSlots) {
        // Subsistema 1 — datos del médico
        Doctor doctor = medicalStaffService.findDoctorById(doctorId);
        DoctorResponse doctorResponse = medicalMapper.toResponse(doctor);

        // Subsistema 2 — horarios configurados
        List<DoctorSchedule> schedules = medicalStaffService.getDoctorSchedule(doctorId);
        List<ScheduleResponse> scheduleResponses = new ArrayList<>();
        for (DoctorSchedule schedule : schedules) {
            scheduleResponses.add(medicalMapper.toResponse(schedule));
        }

        // Subsistema 3 — disponibilidad calculada para la fecha
        List<String> slots = medicalStaffService.getAvailability(
                doctorId, date, occupiedSlots != null ? occupiedSlots : List.of());

        return new DoctorFullInfoResponse(doctorResponse, scheduleResponses, slots);
    }
}
