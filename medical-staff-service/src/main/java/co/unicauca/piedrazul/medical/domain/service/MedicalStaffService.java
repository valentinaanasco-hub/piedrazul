package co.unicauca.piedrazul.medical.domain.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.unicauca.piedrazul.medical.domain.entities.Doctor;
import co.unicauca.piedrazul.medical.domain.entities.DoctorSchedule;
import co.unicauca.piedrazul.medical.domain.factory.AvailabilityGeneratorFactory;
import co.unicauca.piedrazul.medical.domain.factory.AvailabilitySlot;
import co.unicauca.piedrazul.medical.domain.repository.DoctorRepository;
import co.unicauca.piedrazul.medical.domain.repository.DoctorScheduleRepository;
import co.unicauca.piedrazul.medical.domain.repository.OccupiedSlotCacheRepository;
import co.unicauca.piedrazul.medical.domain.repository.SpecialtyRepository;

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
    private final OccupiedSlotCacheRepository  occupiedSlotCacheRepository;

    public MedicalStaffService(
            DoctorRepository doctorRepository,
            DoctorScheduleRepository scheduleRepository,
            SpecialtyRepository specialtyRepository,
            AvailabilityGeneratorFactory generatorFactory,
            OccupiedSlotCacheRepository occupiedSlotCacheRepository) {
        this.doctorRepository    = doctorRepository;
        this.scheduleRepository  = scheduleRepository;
        this.specialtyRepository = specialtyRepository;
        this.generatorFactory    = generatorFactory;
        this.occupiedSlotCacheRepository = occupiedSlotCacheRepository;
    }

    public List<Doctor> listAllDoctors() {
        return mapRowsToDoctors(doctorRepository.findAllWithNames());
    }

    public Doctor findDoctorById(int id) {
        return doctorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Médico no encontrado"));
    }

    public List<Doctor> listDoctorsBySpecialty(int specialtyId) {
        return mapRowsToDoctors(doctorRepository.findBySpecialtyIdWithNames(specialtyId));
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

<<<<<<< HEAD
    /**
     * Genera franjas disponibles para un médico en una fecha.
     * sched_day_of_week: 1=Lunes...7=Domingo 
     */
    public List<String> getAvailability(int doctorId, LocalDate date) {
        findDoctorById(doctorId);

        int dayValue = date.getDayOfWeek().getValue();

        List<String> occupiedSlots = occupiedSlotCacheRepository
                    .findByDoctorIdAndDate(doctorId, date)
                    .stream()
                    .filter(slot -> !"CANCELADA".equals(slot.getStatus()))
                    .map(slot -> slot.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm")))
                    .toList();

=======
    public List<String> getAvailability(int doctorId, LocalDate date, List<String> occupiedSlots) {
        findDoctorById(doctorId);
        int dayValue = date.getDayOfWeek().getValue();
>>>>>>> 8e33411a8f9fefedbb45fe8765d76522ce78f6e7
        return scheduleRepository.findByDoctorId(doctorId).stream()
                .filter(s -> s.getDayOfWeek() == dayValue)
                .findFirst()
                .map(schedule -> {
                    var generator = generatorFactory.getGenerator();
                    return generator.generate(schedule, occupiedSlots)
                            .stream()
                            .filter(AvailabilitySlot::isAvailable)
                            .map(AvailabilitySlot::getTime)
                            .toList();
                })
                .orElse(List.of());
    }

    // --- Mapeo de filas nativas a entidades Doctor ---
    private List<Doctor> mapRowsToDoctors(List<Object[]> rows) {
        return rows.stream().map(row -> {
            Doctor doctor = new Doctor();
            doctor.setId(((Number) row[0]).intValue());
            doctor.setLicenseNumber((String) row[1]);
            doctor.setFirstName((String) row[2]);
            doctor.setMiddleName(row[3] != null ? (String) row[3] : null);
            doctor.setFirstSurname((String) row[4]);
            doctor.setLastName(row[5] != null ? (String) row[5] : null);
            // Cargar especialidades
            doctor.setSpecialties(
                    doctorRepository.findById(doctor.getId())
                            .map(Doctor::getSpecialties)
                            .orElse(List.of())
            );
            return doctor;
        }).toList();
    }
}