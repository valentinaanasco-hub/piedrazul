package co.unicauca.piedrazul.medical;

import co.unicauca.piedrazul.medical.domain.entities.Doctor;
import co.unicauca.piedrazul.medical.domain.entities.DoctorSchedule;
import co.unicauca.piedrazul.medical.domain.factory.AvailabilityGeneratorFactory;
import co.unicauca.piedrazul.medical.domain.factory.StandardAvailabilityGenerator;
import co.unicauca.piedrazul.medical.domain.repository.DoctorRepository;
import co.unicauca.piedrazul.medical.domain.repository.DoctorScheduleRepository;
import co.unicauca.piedrazul.medical.domain.repository.SpecialtyRepository;
import co.unicauca.piedrazul.medical.domain.service.MedicalStaffService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para MedicalStaffService.
 * Verifica el patrón Factory Method para generación de disponibilidad.
 *
 * @author Ginner Ortega
 */
@ExtendWith(MockitoExtension.class)
class MedicalStaffServiceTest {

    @Mock private DoctorRepository         doctorRepository;
    @Mock private DoctorScheduleRepository scheduleRepository;
    @Mock private SpecialtyRepository      specialtyRepository;

    private MedicalStaffService service;
    private Doctor              validDoctor;
    private DoctorSchedule      mondaySchedule;

    @BeforeEach
    void setUp() {
        StandardAvailabilityGenerator generator = new StandardAvailabilityGenerator();
        AvailabilityGeneratorFactory  factory   = new AvailabilityGeneratorFactory(generator);

        service = new MedicalStaffService(
                doctorRepository, scheduleRepository, specialtyRepository, factory
        );

        validDoctor = new Doctor();
        validDoctor.setId(1);
        validDoctor.setFirstName("María");
        validDoctor.setFirstSurname("García");
        validDoctor.setLicenseNumber("MED-001");

        mondaySchedule = new DoctorSchedule();
        mondaySchedule.setId(1);
        mondaySchedule.setDoctor(validDoctor);
        mondaySchedule.setDayOfWeek(1);  // 1 = Lunes (ISO 8601)
        mondaySchedule.setStartTime(LocalTime.of(8, 0));
        mondaySchedule.setEndTime(LocalTime.of(10, 0));
        mondaySchedule.setIntervalMinutes(30);
    }

    // --- Tests de médicos ---

    @Test
    void listAllDoctors_retornaLista() {
        when(doctorRepository.findAll()).thenReturn(List.of(validDoctor));
        assertEquals(1, service.listAllDoctors().size());
    }

    @Test
    void findDoctorById_idExistente_retornaDoctor() {
        when(doctorRepository.findById(1)).thenReturn(Optional.of(validDoctor));
        Doctor result = service.findDoctorById(1);
        assertNotNull(result);
        assertEquals(1, result.getId());
    }

    @Test
    void findDoctorById_idNoExistente_lanzaExcepcion() {
        when(doctorRepository.findById(anyInt())).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.findDoctorById(99));
    }

    // --- Tests de disponibilidad ---

    @Test
    void getAvailability_lunes_retornaFranjasCorrectas() {
        when(doctorRepository.findById(1)).thenReturn(Optional.of(validDoctor));
        when(scheduleRepository.findByDoctorId(1)).thenReturn(List.of(mondaySchedule));

        // 2026-05-11 es Lunes → DayOfWeek.getValue() = 1
        List<String> slots = service.getAvailability(1, LocalDate.of(2026, 5, 11), List.of());

        // 08:00 a 10:00 cada 30min → 08:00, 08:30, 09:00, 09:30
        assertEquals(4, slots.size());
        assertTrue(slots.contains("08:00"));
        assertTrue(slots.contains("09:30"));
    }

    @Test
    void getAvailability_conFranjasOcupadas_excluyeOcupadas() {
        when(doctorRepository.findById(1)).thenReturn(Optional.of(validDoctor));
        when(scheduleRepository.findByDoctorId(1)).thenReturn(List.of(mondaySchedule));

        List<String> slots = service.getAvailability(
                1, LocalDate.of(2026, 5, 11), List.of("08:00", "09:00")
        );

        assertEquals(2, slots.size());
        assertFalse(slots.contains("08:00"));
        assertFalse(slots.contains("09:00"));
        assertTrue(slots.contains("08:30"));
        assertTrue(slots.contains("09:30"));
    }

    @Test
    void getAvailability_diaNoTrabajado_retornaVacio() {
        when(doctorRepository.findById(1)).thenReturn(Optional.of(validDoctor));
        when(scheduleRepository.findByDoctorId(1)).thenReturn(List.of(mondaySchedule));

        // 2026-05-10 es Domingo → DayOfWeek.getValue() = 7
        List<String> slots = service.getAvailability(1, LocalDate.of(2026, 5, 10), List.of());

        assertTrue(slots.isEmpty());
    }

    @Test
    void getAvailability_doctorNoExiste_lanzaExcepcion() {
        when(doctorRepository.findById(anyInt())).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class,
                () -> service.getAvailability(99, LocalDate.now(), List.of()));
    }

    // --- Tests de horarios ---

    @Test
    void getDoctorSchedule_retornaHorarios() {
        when(doctorRepository.findById(1)).thenReturn(Optional.of(validDoctor));
        when(scheduleRepository.findByDoctorId(1)).thenReturn(List.of(mondaySchedule));

        List<DoctorSchedule> result = service.getDoctorSchedule(1);
        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getDayOfWeek()); // 1 = Lunes
    }
}