package co.unicauca.piedrazul.medical;

import co.unicauca.piedrazul.medical.domain.entities.Doctor;
import co.unicauca.piedrazul.medical.domain.entities.DoctorSchedule;
import co.unicauca.piedrazul.medical.domain.entities.OccupiedSlotCache;
import co.unicauca.piedrazul.medical.domain.factory.AvailabilityGeneratorFactory;
import co.unicauca.piedrazul.medical.domain.factory.StandardGeneratorFactory;
import co.unicauca.piedrazul.medical.domain.repository.DoctorRepository;
import co.unicauca.piedrazul.medical.domain.repository.DoctorScheduleRepository;
import co.unicauca.piedrazul.medical.domain.repository.OccupiedSlotCacheRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para MedicalStaffService.
 * Verifica el patrón Factory Method para generación de disponibilidad.
 *
 * @author Ginner Ortega
 */
@ExtendWith(MockitoExtension.class)
class MedicalStaffServiceTest {

    @Mock private DoctorRepository            doctorRepository;
    @Mock private DoctorScheduleRepository    scheduleRepository;
    @Mock private OccupiedSlotCacheRepository occupiedSlotCacheRepository;

    private MedicalStaffService service;
    private Doctor              validDoctor;
    private DoctorSchedule      mondaySchedule;

    @BeforeEach
    void setUp() {
        // StandardGeneratorFactory es el Concrete Creator del patrón Factory Method
        AvailabilityGeneratorFactory factory = new StandardGeneratorFactory();

        service = new MedicalStaffService(
                doctorRepository, scheduleRepository,
                factory, occupiedSlotCacheRepository
        );

        validDoctor = new Doctor();
        validDoctor.setId(1);
        validDoctor.setFirstName("María");
        validDoctor.setFirstSurname("García");
        validDoctor.setLicenseNumber("MED-001");

        mondaySchedule = new DoctorSchedule();
        mondaySchedule.setId(1);
        mondaySchedule.setDoctor(validDoctor);
        mondaySchedule.setDayOfWeek(1); // 1 = Lunes (ISO 8601)
        mondaySchedule.setStartTime(LocalTime.of(8, 0));
        mondaySchedule.setEndTime(LocalTime.of(10, 0));
        mondaySchedule.setIntervalMinutes(30);
    }

    // --- Tests de médicos ---

    @Test
    void listAllDoctors_retornaLista() {
        when(doctorRepository.findAllWithNames()).thenReturn(List.of());
        assertNotNull(service.listAllDoctors());
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
        when(occupiedSlotCacheRepository.findByDoctorIdAndDate(anyInt(), any()))
                .thenReturn(List.of());

        // 2026-05-11 es Lunes
        List<String> slots = service.getAvailability(1, LocalDate.of(2026, 5, 11));

        // 08:00 a 10:00 cada 30min → 4 franjas
        assertEquals(4, slots.size());
        assertTrue(slots.contains("08:00"));
        assertTrue(slots.contains("09:30"));
    }

    @Test
    void getAvailability_conFranjasOcupadas_excluyeOcupadas() {
        when(doctorRepository.findById(1)).thenReturn(Optional.of(validDoctor));
        when(scheduleRepository.findByDoctorId(1)).thenReturn(List.of(mondaySchedule));

        // Simular dos citas ocupadas en Redis
        OccupiedSlotCache slot1 = new OccupiedSlotCache();
        slot1.setDoctorId(1);
        slot1.setDate(LocalDate.of(2026, 5, 11));
        slot1.setStartTime(LocalTime.of(8, 0));
        slot1.setStatus("AGENDADA");

        OccupiedSlotCache slot2 = new OccupiedSlotCache();
        slot2.setDoctorId(1);
        slot2.setDate(LocalDate.of(2026, 5, 11));
        slot2.setStartTime(LocalTime.of(9, 0));
        slot2.setStatus("AGENDADA");

        when(occupiedSlotCacheRepository.findByDoctorIdAndDate(anyInt(), any()))
                .thenReturn(List.of(slot1, slot2));

        List<String> slots = service.getAvailability(1, LocalDate.of(2026, 5, 11));

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
        when(occupiedSlotCacheRepository.findByDoctorIdAndDate(anyInt(), any()))
                .thenReturn(List.of());

        // 2026-05-10 es Domingo
        List<String> slots = service.getAvailability(1, LocalDate.of(2026, 5, 10));
        assertTrue(slots.isEmpty());
    }

    @Test
    void getAvailability_doctorNoExiste_lanzaExcepcion() {
        when(doctorRepository.findById(anyInt())).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class,
                () -> service.getAvailability(99, LocalDate.now()));
    }

    // --- Tests de horarios ---

    @Test
    void getDoctorSchedule_retornaHorarios() {
        when(doctorRepository.findById(1)).thenReturn(Optional.of(validDoctor));
        when(scheduleRepository.findByDoctorId(1)).thenReturn(List.of(mondaySchedule));

        List<DoctorSchedule> result = service.getDoctorSchedule(1);
        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getDayOfWeek());
    }
}