package co.unicauca.piedrazul.domain.services;

import co.unicauca.piedrazul.domain.access.IAppointmentRepository;
import co.unicauca.piedrazul.domain.access.IDoctorScheduleRepository;
import co.unicauca.piedrazul.domain.entities.Appointment;
import co.unicauca.piedrazul.domain.entities.DoctorSchedule;
import co.unicauca.piedrazul.domain.entities.enums.AppointmentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para AvailabilityService.
 *
 * Cubre los casos críticos del cálculo de slots disponibles:
 * horarios configurados, citas ocupadas, citas canceladas y días sin configuración.
 */
@ExtendWith(MockitoExtension.class)
class AvailabilityServiceTest {

    @Mock
    private IDoctorScheduleRepository scheduleRepository;

    @Mock
    private IAppointmentRepository appointmentRepository;

    private AvailabilityService availabilityService;

    // Lunes 2025-06-02 (dayOfWeek = 1)
    private static final LocalDate LUNES = LocalDate.of(2025, 6, 2);
    private static final int DOCTOR_ID = 1;

    @BeforeEach
    void setUp() {
        availabilityService = new AvailabilityService(scheduleRepository, appointmentRepository);
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private DoctorSchedule buildSchedule(int dayOfWeek, LocalTime start, LocalTime end, int interval) {
        DoctorSchedule s = new DoctorSchedule();
        s.setDayOfWeek(dayOfWeek);
        s.setStartTime(start);
        s.setEndTime(end);
        s.setIntervalMinutes(interval);
        return s;
    }

    private Appointment buildAppointment(LocalTime start, AppointmentStatus status) {
        Appointment a = new Appointment();
        a.setStartTime(start);
        a.setStatus(status);
        return a;
    }

    // -----------------------------------------------------------------------
    // Test 1: Horario de 8:00 a 10:00 con intervalo de 30 min → 4 slots exactos
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Horario 08:00-10:00 con intervalo 30min y sin citas -> exactamente 4 slots")
    void getAvailableSlots_horario2horas_intervalo30min_retorna4Slots() {
        // GIVEN: horario lunes 08:00-10:00, intervalo 30 min
        DoctorSchedule schedule = buildSchedule(1, LocalTime.of(8, 0), LocalTime.of(10, 0), 30);
        when(scheduleRepository.findByDoctorId(DOCTOR_ID)).thenReturn(List.of(schedule));
        when(appointmentRepository.findByDoctorAndDate(DOCTOR_ID, LUNES.toString()))
                .thenReturn(Collections.emptyList());

        // WHEN
        List<LocalTime> slots = availabilityService.getAvailableSlots(DOCTOR_ID, LUNES);

        // THEN: debe retornar exactamente [08:00, 08:30, 09:00, 09:30]
        assertEquals(4, slots.size(), "Deben existir exactamente 4 slots");
        assertTrue(slots.contains(LocalTime.of(8, 0)));
        assertTrue(slots.contains(LocalTime.of(8, 30)));
        assertTrue(slots.contains(LocalTime.of(9, 0)));
        assertTrue(slots.contains(LocalTime.of(9, 30)));
    }

    // -----------------------------------------------------------------------
    // Test 2: Cita AGENDADA a las 09:00 → ese slot se excluye, quedan solo 3
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Cita AGENDADA a las 09:00 -> ese slot se excluye, retorna solo 3 slots")
    void getAvailableSlots_citaAgendadaA09_excluyeEseSlot_retorna3() {
        // GIVEN: horario 08:00-10:00 con intervalo 30 min
        DoctorSchedule schedule = buildSchedule(1, LocalTime.of(8, 0), LocalTime.of(10, 0), 30);
        when(scheduleRepository.findByDoctorId(DOCTOR_ID)).thenReturn(List.of(schedule));

        // La cita de las 09:00 ya está ocupada
        Appointment citaOcupada = buildAppointment(LocalTime.of(9, 0), AppointmentStatus.AGENDADA);
        when(appointmentRepository.findByDoctorAndDate(DOCTOR_ID, LUNES.toString()))
                .thenReturn(List.of(citaOcupada));

        // WHEN
        List<LocalTime> slots = availabilityService.getAvailableSlots(DOCTOR_ID, LUNES);

        // THEN: 3 slots disponibles, el de las 09:00 NO aparece
        assertEquals(3, slots.size(), "Deben quedar 3 slots disponibles");
        assertFalse(slots.contains(LocalTime.of(9, 0)), "El slot 09:00 debe estar bloqueado");
        assertTrue(slots.contains(LocalTime.of(8, 0)));
        assertTrue(slots.contains(LocalTime.of(8, 30)));
        assertTrue(slots.contains(LocalTime.of(9, 30)));
    }

    // -----------------------------------------------------------------------
    // Test 3: Día sin horario configurado → lista vacía
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Fecha en día no configurado para el doctor -> retorna lista vacía")
    void getAvailableSlots_diaNoConfigurado_retornaListaVacia() {
        // GIVEN: horario solo para lunes (dayOfWeek=1), pero consultamos martes
        DoctorSchedule scheduleParaLunes = buildSchedule(1, LocalTime.of(8, 0), LocalTime.of(10, 0), 30);
        when(scheduleRepository.findByDoctorId(DOCTOR_ID)).thenReturn(List.of(scheduleParaLunes));

        LocalDate martes = LocalDate.of(2025, 6, 3); // dayOfWeek = 2
        when(appointmentRepository.findByDoctorAndDate(DOCTOR_ID, martes.toString()))
                .thenReturn(Collections.emptyList());

        // WHEN
        List<LocalTime> slots = availabilityService.getAvailableSlots(DOCTOR_ID, martes);

        // THEN: sin horario para ese día → sin slots
        assertTrue(slots.isEmpty(), "Un día sin horario configurado no debe tener slots disponibles");
    }

    // -----------------------------------------------------------------------
    // Test 4 (bonus): Cita CANCELADA libera su slot — vuelve a aparecer disponible
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Cita CANCELADA -> su slot vuelve a estar disponible")
    void getAvailableSlots_citaCancelada_slotQuedaLibre() {
        DoctorSchedule schedule = buildSchedule(1, LocalTime.of(8, 0), LocalTime.of(9, 0), 30);
        when(scheduleRepository.findByDoctorId(DOCTOR_ID)).thenReturn(List.of(schedule));

        // La cita está cancelada → no debe bloquear el slot
        Appointment citaCancelada = buildAppointment(LocalTime.of(8, 0), AppointmentStatus.CANCELADA);
        when(appointmentRepository.findByDoctorAndDate(DOCTOR_ID, LUNES.toString()))
                .thenReturn(List.of(citaCancelada));

        List<LocalTime> slots = availabilityService.getAvailableSlots(DOCTOR_ID, LUNES);

        assertEquals(2, slots.size(), "Las citas canceladas deben liberar su slot");
        assertTrue(slots.contains(LocalTime.of(8, 0)));
    }

    // -----------------------------------------------------------------------
    // Test 5 (bonus): Lista de citas null desde el repositorio → no explota
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Repositorio retorna null en lugar de lista -> no lanza NullPointerException")
    void getAvailableSlots_repositorioDevuelveNull_noLanzaExcepcion() {
        DoctorSchedule schedule = buildSchedule(1, LocalTime.of(8, 0), LocalTime.of(8, 30), 30);
        when(scheduleRepository.findByDoctorId(DOCTOR_ID)).thenReturn(List.of(schedule));
        when(appointmentRepository.findByDoctorAndDate(DOCTOR_ID, LUNES.toString()))
                .thenReturn(null); // Caso defensivo

        List<LocalTime> slots = assertDoesNotThrow(
                () -> availabilityService.getAvailableSlots(DOCTOR_ID, LUNES),
                "El servicio no debe lanzar NullPointerException si el repositorio retorna null"
        );
        assertEquals(1, slots.size());
    }

    // -----------------------------------------------------------------------
    // Test 6: getIntervalMinutesForDoctorOnDate — horario existente
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Intervalo configurado para el día -> retorna el valor correcto")
    void getIntervalMinutes_horarioExisteParaElDia_retornaIntervalo() {
        DoctorSchedule schedule = buildSchedule(1, LocalTime.of(8, 0), LocalTime.of(12, 0), 20);
        when(scheduleRepository.findByDoctorId(DOCTOR_ID)).thenReturn(List.of(schedule));

        int interval = availabilityService.getIntervalMinutesForDoctorOnDate(DOCTOR_ID, LUNES);

        assertEquals(20, interval, "Debe retornar el intervalo configurado de 20 minutos");
    }

    // -----------------------------------------------------------------------
    // Test 7: getIntervalMinutesForDoctorOnDate — sin horario → valor por defecto 30
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Sin horario configurado para el día -> retorna 30 como valor por defecto")
    void getIntervalMinutes_sinHorarioParaElDia_retorna30PorDefecto() {
        when(scheduleRepository.findByDoctorId(DOCTOR_ID)).thenReturn(Collections.emptyList());

        int interval = availabilityService.getIntervalMinutesForDoctorOnDate(DOCTOR_ID, LUNES);

        assertEquals(30, interval, "El valor por defecto debe ser 30 minutos");
    }
}