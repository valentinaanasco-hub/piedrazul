package co.unicauca.piedrazul.medical.presentation.mapper;

import co.unicauca.piedrazul.medical.domain.entities.Doctor;
import co.unicauca.piedrazul.medical.domain.entities.DoctorSchedule;
import co.unicauca.piedrazul.medical.domain.entities.Specialty;
import co.unicauca.piedrazul.medical.presentation.dto.MedicalDTOs.*;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.util.List;

/**
 * Mapper para conversión entre entidades y DTOs.
 *
 * @author Ginner Ortega
 */
@Component
public class MedicalMapper {

    public DoctorResponse toResponse(Doctor doctor) {
        List<String> specialties = doctor.getSpecialties() != null
                ? doctor.getSpecialties().stream().map(Specialty::getName).toList()
                : List.of();

        return new DoctorResponse(
                doctor.getId(),
                doctor.getFullName(),
                doctor.getLicenseNumber(),
                specialties
        );
    }

    public ScheduleResponse toResponse(DoctorSchedule schedule) {
        return new ScheduleResponse(
                schedule.getId(),
                schedule.getDayName(),
                schedule.getDayOfWeek(),
                schedule.getStartTime().toString(),
                schedule.getEndTime().toString(),
                schedule.getIntervalMinutes()
        );
    }

    public SpecialtyResponse toResponse(Specialty specialty) {
        return new SpecialtyResponse(specialty.getId(), specialty.getName());
    }

    public List<DoctorSchedule> toEntities(ScheduleUpdateRequest request) {
        return request.selectedDays().stream()
                .map(day -> {
                    DoctorSchedule schedule = new DoctorSchedule();
                    schedule.setDayOfWeek(day);
                    schedule.setStartTime(LocalTime.parse(request.startTime()));
                    schedule.setEndTime(LocalTime.parse(request.endTime()));
                    schedule.setIntervalMinutes(request.interval());
                    return schedule;
                })
                .toList();
    }
}