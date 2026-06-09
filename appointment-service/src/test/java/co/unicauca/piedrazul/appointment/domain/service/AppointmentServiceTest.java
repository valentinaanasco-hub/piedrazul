package co.unicauca.piedrazul.appointment.domain.service;

import co.unicauca.piedrazul.appointment.domain.exception.AppointmentNotFoundException;
import co.unicauca.piedrazul.appointment.domain.model.Appointment;
import co.unicauca.piedrazul.appointment.domain.model.AppointmentStatus;
import co.unicauca.piedrazul.appointment.domain.port.out.AppointmentEventPort;
import co.unicauca.piedrazul.appointment.domain.port.out.AppointmentRepositoryPort;
import co.unicauca.piedrazul.appointment.domain.port.out.PatientAuthorizationPort;
import co.unicauca.piedrazul.appointment.domain.service.template.ManualAppointmentScheduling;
import co.unicauca.piedrazul.appointment.domain.service.template.RescheduleAppointmentScheduling;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para AppointmentService.
 * Verifica la lógica de orquestación del servicio de citas.
 */
@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock private AppointmentRepositoryPort       repositoryPort;
    @Mock private AppointmentEventPort            eventPort;
    @Mock private ManualAppointmentScheduling     manualScheduling;
    @Mock private RescheduleAppointmentScheduling rescheduleScheduling;
    @Mock private PatientAuthorizationPort        authorizationPort;

    private AppointmentService appointmentService;

    @BeforeEach
    void setUp() {
        appointmentService = new AppointmentService(
                repositoryPort, eventPort, manualScheduling, rescheduleScheduling, authorizationPort);
    }

    private Appointment buildAppointment(int id, AppointmentStatus status) {
        Appointment appointment = new Appointment();
        appointment.setAppointmentId(id);
        appointment.setDoctorId(1);
        appointment.setPatientId(2);
        appointment.setDate(LocalDate.of(2026, 6, 10));
        appointment.setStartTime(LocalTime.of(9, 0));
        appointment.setEndTime(LocalTime.of(9, 30));
        appointment.setStatus(status);
        return appointment;
    }

    // --- scheduleAppointment ---

    @Test
    void scheduleAppointment_delegaAlTemplateMethod() {
        Appointment input = buildAppointment(0, AppointmentStatus.AGENDADA);
        Appointment saved = buildAppointment(1, AppointmentStatus.AGENDADA);

        when(manualScheduling.execute(any())).thenReturn(saved);

        Appointment result = appointmentService.scheduleAppointment(input);

        assertNotNull(result);
        assertEquals(1, result.getAppointmentId());
        verify(manualScheduling).execute(input);
    }

    // --- cancelAppointment ---

    @Test
    void cancelAppointment_citaExistente_cambiaEstadoACancelada() {
        Appointment appointment = buildAppointment(5, AppointmentStatus.AGENDADA);
        when(repositoryPort.findById(5)).thenReturn(Optional.of(appointment));
        when(repositoryPort.save(any())).thenReturn(appointment);

        Appointment result = appointmentService.cancelAppointment(5);

        assertEquals(AppointmentStatus.CANCELADA, result.getStatus());
        verify(repositoryPort).save(appointment);
        verify(eventPort).publishAppointmentEvent(appointment);
    }

    @Test
    void cancelAppointment_citaNoExiste_lanzaAppointmentNotFoundException() {
        when(repositoryPort.findById(99)).thenReturn(Optional.empty());

        assertThrows(AppointmentNotFoundException.class,
                () -> appointmentService.cancelAppointment(99));
    }

    // --- markAsAttended ---

    @Test
    void markAsAttended_citaExistente_cambiaEstadoAAtendida() {
        Appointment appointment = buildAppointment(3, AppointmentStatus.AGENDADA);
        when(repositoryPort.findById(3)).thenReturn(Optional.of(appointment));
        when(repositoryPort.save(any())).thenReturn(appointment);

        Appointment result = appointmentService.markAsAttended(3, null);

        assertEquals(AppointmentStatus.ATENDIDA, result.getStatus());
        verify(eventPort).publishAppointmentEvent(appointment);
    }

    // --- findById ---

    @Test
    void findById_citaExistente_retornaCita() {
        Appointment appointment = buildAppointment(7, AppointmentStatus.AGENDADA);
        when(repositoryPort.findById(7)).thenReturn(Optional.of(appointment));

        Appointment result = appointmentService.findById(7);

        assertNotNull(result);
        assertEquals(7, result.getAppointmentId());
    }

    @Test
    void findById_citaNoExiste_lanzaAppointmentNotFoundException() {
        when(repositoryPort.findById(99)).thenReturn(Optional.empty());

        AppointmentNotFoundException ex = assertThrows(
                AppointmentNotFoundException.class,
                () -> appointmentService.findById(99)
        );
        assertEquals("Cita no encontrada con ID: 99", ex.getMessage());
    }

    // --- listByDoctorAndDate ---

    @Test
    void listByDoctorAndDate_retornaListaDeCitas() {
        LocalDate date = LocalDate.of(2026, 6, 10);
        List<Appointment> appointments = List.of(
                buildAppointment(1, AppointmentStatus.AGENDADA),
                buildAppointment(2, AppointmentStatus.AGENDADA)
        );
        when(repositoryPort.findByDoctorIdAndDate(1, date)).thenReturn(appointments);

        List<Appointment> result = appointmentService.listByDoctorAndDate(1, date);

        assertEquals(2, result.size());
    }

    // --- listAll ---

    @Test
    void listAll_retornaTodasLasCitas() {
        List<Appointment> all = List.of(
                buildAppointment(1, AppointmentStatus.AGENDADA),
                buildAppointment(2, AppointmentStatus.CANCELADA),
                buildAppointment(3, AppointmentStatus.ATENDIDA)
        );
        when(repositoryPort.findAll()).thenReturn(all);

        List<Appointment> result = appointmentService.listAll();

        assertEquals(3, result.size());
    }
}
