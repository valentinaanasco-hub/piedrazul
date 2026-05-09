package co.unicauca.piedrazul.medical.domain.service;

import co.unicauca.piedrazul.medical.domain.entities.Doctor;
import co.unicauca.piedrazul.medical.domain.entities.DoctorSchedule;
import co.unicauca.piedrazul.medical.domain.factory.AvailabilityGeneratorFactory;
import co.unicauca.piedrazul.medical.domain.factory.AvailabilitySlot;
import co.unicauca.piedrazul.medical.domain.repository.DoctorRepository;
import co.unicauca.piedrazul.medical.domain.repository.DoctorScheduleRepository;
import co.unicauca.piedrazul.medical.domain.repository.SpecialtyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Servicio de dominio para gestión del personal médico.
 * Usa el patrón Factory Method para generar disponibilidad.
 *
 * @author Ginner Ortega
 */
@Service
public class MedicalStaffService {

    private final DoctorRepository             doctorRepository;
    private final DoctorScheduleRepository     scheduleRepository;
    private final SpecialtyRepository          specialtyRepository;
    private final AvailabilityGeneratorFactory generatorFactory;

    public MedicalStaffService(
            DoctorRepository doctorRepository,
            DoctorScheduleRepository scheduleRepository,
            SpecialtyRepository specialtyRepository,
            AvailabilityGeneratorFactory generatorFactory) {
        this.doctorRepository    = doctorRepository;
        this.scheduleRepository  = scheduleRepository;
        this.specialtyRepository = specialtyRepository;
        this.generatorFactory    = generatorFactory;
    }

    public List<Doctor> listAllDoctors() {
        return doctorRepository.findAll();
    }

    public Doctor findDoctorById(int id) {
        return doctorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Médico no encontrado"));
    }

    public List<Doctor> listDoctorsBySpecialty(int specialtyId) {
        return doctorRepository.findBySpecialtyId(specialtyId);
    }

    public List<DoctorSchedule> getDoctorSchedule(int doctorId) {
        findDoctorById(doctorId);
        return scheduleRepository.findByDoctorId(doctorId);
    }

    @Transactional
    public List<DoctorSchedule> updateSchedule(int doctorId, List<DoctorSchedule> newSchedules) {
        Doctor doctor = findDoctorById(doctorId);
        scheduleRepository.deleteByDoctorId(doctorId);
        newSchedules.forEach(s -> s.setDoctor(doctor));
        return scheduleRepository.saveAll(newSchedules);
    }

    /**
     * Genera franjas disponibles para un médico en una fecha.
     * sched_day_of_week: 1=Lunes...7=Domingo (ISO 8601 = DayOfWeek.getValue())
     */
    public List<String> getAvailability(int doctorId, LocalDate date, List<String> occupiedSlots) {
        findDoctorById(doctorId);

        int dayValue = date.getDayOfWeek().getValue(); // 1=Lunes...7=Domingo

        return scheduleRepository.findByDoctorId(doctorId).stream()
                .filter(s -> s.getDayOfWeek() == dayValue)
                .findFirst()
                .map(schedule -> {
                    var generator = generatorFactory.getGenerator("STANDARD");
                    return generator.generate(schedule, occupiedSlots)
                            .stream()
                            .filter(AvailabilitySlot::isAvailable)
                            .map(AvailabilitySlot::getTime)
                            .toList();
                })
                .orElse(List.of());
    }
}