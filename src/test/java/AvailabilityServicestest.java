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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para AvailabilityService.
 *
 * Se usan mocks de los repositorios para que las pruebas no necesiten
 * base de datos y sean rápidas y repetibles.
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
    // Helpers para construir objetos de prueba
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
    // Pruebas de getAvailableSlots
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Sin horario configurado -> lista vacía de slots")
    void getAvailableSlots_sinHorario_retornaListaVacia() {
        when(scheduleRepository.findByDoctorId(DOCTOR_ID)).thenReturn(Collections.emptyList());
        when(appointmentRepository.findByDoctorAndDate(DOCTOR_ID, LUNES.toString()))
                .thenReturn(Collections.emptyList());

        List<LocalTime> slots = availabilityService.getAvailableSlots(DOCTOR_ID, LUNES);

        assertTrue(slots.isEmpty(), "Sin horario no debe haber slots disponibles");
    }

    @Test
    @DisplayName("Horario de lunes configurado, sin citas -> todos los slots disponibles")
    void getAvailableSlots_sinCitas_retornaTodosLosSlots() {
        // Horario: lunes 08:00 - 09:00 con intervalo 30 min -> slots: 08:00 y 08:30
        DoctorSchedule schedule = buildSchedule(1, LocalTime.of(8, 0), LocalTime.of(9, 0), 30);
        when(scheduleRepository.findByDoctorId(DOCTOR_ID)).thenReturn(List.of(schedule));
        when(appointmentRepository.findByDoctorAndDate(DOCTOR_ID, LUNES.toString()))
                .thenReturn(Collections.emptyList());

        List<LocalTime> slots = availabilityService.getAvailableSlots(DOCTOR_ID, LUNES);

        assertEquals(2, slots.size());
        assertTrue(slots.contains(LocalTime.of(8, 0)));
        assertTrue(slots.contains(LocalTime.of(8, 30)));
    }

    @Test
    @DisplayName("Una cita AGENDADA ocupa su slot -> ese horario no aparece disponible")
    void getAvailableSlots_conCitaAgendada_slotOcupadoNoDisponible() {
        DoctorSchedule schedule = buildSchedule(1, LocalTime.of(8, 0), LocalTime.of(9, 0), 30);
        when(scheduleRepository.findByDoctorId(DOCTOR_ID)).thenReturn(List.of(schedule));

        // La cita de las 08:00 ya está agendada
        Appointment cita = buildAppointment(LocalTime.of(8, 0), AppointmentStatus.AGENDADA);
        when(appointmentRepository.findByDoctorAndDate(DOCTOR_ID, LUNES.toString()))
                .thenReturn(List.of(cita));

        List<LocalTime> slots = availabilityService.getAvailableSlots(DOCTOR_ID, LUNES);

        assertEquals(1, slots.size());
        assertTrue(slots.contains(LocalTime.of(8, 30)), "El slot 08:30 debe estar libre");
        assertFalse(slots.contains(LocalTime.of(8, 0)), "El slot 08:00 debe estar ocupado");
    }

    @Test
    @DisplayName("Una cita CANCELADA NO ocupa su slot -> ese horario vuelve a estar disponible")
    void getAvailableSlots_conCitaCancelada_slotLibre() {
        DoctorSchedule schedule = buildSchedule(1, LocalTime.of(8, 0), LocalTime.of(9, 0), 30);
        when(scheduleRepository.findByDoctorId(DOCTOR_ID)).thenReturn(List.of(schedule));

        // La cita de las 08:00 está cancelada -> debe liberar el slot
        Appointment citaCancelada = buildAppointment(LocalTime.of(8, 0), AppointmentStatus.CANCELADA);
        when(appointmentRepository.findByDoctorAndDate(DOCTOR_ID, LUNES.toString()))
                .thenReturn(List.of(citaCancelada));

        List<LocalTime> slots = availabilityService.getAvailableSlots(DOCTOR_ID, LUNES);

        assertEquals(2, slots.size(), "Los slots de citas canceladas deben quedar disponibles");
        assertTrue(slots.contains(LocalTime.of(8, 0)));
    }

    @Test
    @DisplayName("Fecha en día diferente al horario configurado -> lista vacía")
    void getAvailableSlots_diaNoConfigurado_retornaListaVacia() {
        // Horario solo configurado para lunes (dayOfWeek=1)
        DoctorSchedule schedule = buildSchedule(1, LocalTime.of(8, 0), LocalTime.of(9, 0), 30);
        when(scheduleRepository.findByDoctorId(DOCTOR_ID)).thenReturn(List.of(schedule));

        // Martes 2025-06-03 (dayOfWeek=2) no tiene horario
        LocalDate martes = LocalDate.of(2025, 6, 3);
        when(appointmentRepository.findByDoctorAndDate(DOCTOR_ID, martes.toString()))
                .thenReturn(Collections.emptyList());

        List<LocalTime> slots = availabilityService.getAvailableSlots(DOCTOR_ID, martes);

        assertTrue(slots.isEmpty(), "Un día sin horario configurado no debe tener slots");
    }

    @Test
    @DisplayName("Lista de citas nula -> no lanza excepción y retorna todos los slots")
    void getAvailableSlots_citasNulas_noLanzaExcepcion() {
        DoctorSchedule schedule = buildSchedule(1, LocalTime.of(8, 0), LocalTime.of(8, 30), 30);
        when(scheduleRepository.findByDoctorId(DOCTOR_ID)).thenReturn(List.of(schedule));
        when(appointmentRepository.findByDoctorAndDate(DOCTOR_ID, LUNES.toString()))
                .thenReturn(null); // Simula repositorio que devuelve null

        List<LocalTime> slots = assertDoesNotThrow(
                () -> availabilityService.getAvailableSlots(DOCTOR_ID, LUNES)
        );
        assertEquals(1, slots.size());
    }

    // -----------------------------------------------------------------------
    // Pruebas de getIntervalMinutesForDoctorOnDate
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Horario configurado para el día -> retorna el intervalo correcto")
    void getIntervalMinutes_horarioExiste_retornaIntervalo() {
        DoctorSchedule schedule = buildSchedule(1, LocalTime.of(8, 0), LocalTime.of(12, 0), 20);
        when(scheduleRepository.findByDoctorId(DOCTOR_ID)).thenReturn(List.of(schedule));

        int interval = availabilityService.getIntervalMinutesForDoctorOnDate(DOCTOR_ID, LUNES);

        assertEquals(20, interval);
    }

    @Test
    @DisplayName("Sin horario para el día -> retorna valor por defecto de 30 minutos")
    void getIntervalMinutes_sinHorario_retornaDefault30() {
        when(scheduleRepository.findByDoctorId(DOCTOR_ID)).thenReturn(Collections.emptyList());

        int interval = availabilityService.getIntervalMinutesForDoctorOnDate(DOCTOR_ID, LUNES);

        assertEquals(30, interval, "Debe retornar 30 como intervalo por defecto");
    }
}