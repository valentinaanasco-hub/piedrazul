package co.unicauca.piedrazul.domain.services;

import co.unicauca.piedrazul.domain.access.IAppointmentRepository;
import co.unicauca.piedrazul.domain.access.IDoctorRepository;
import co.unicauca.piedrazul.domain.access.IPatientRepository;
import co.unicauca.piedrazul.domain.entities.Appointment;
import co.unicauca.piedrazul.domain.entities.Doctor;
import co.unicauca.piedrazul.domain.entities.Patient;
import co.unicauca.piedrazul.domain.entities.enums.AppointmentStatus;
import co.unicauca.piedrazul.domain.services.interfaces.IManualAppointmentValidator;
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
 * Pruebas unitarias para ManualAppointmentService.
 */
@ExtendWith(MockitoExtension.class)
class ManualAppointmentServiceTest {

    @Mock
    private IAppointmentRepository appointmentRepository;

    @Mock
    private IDoctorRepository doctorRepository;

    @Mock
    private IPatientRepository patientRepository;

    @Mock
    private IManualAppointmentValidator validator;

    private ManualAppointmentService service;

    @BeforeEach
    void setUp() {
        service = new ManualAppointmentService(
                appointmentRepository, doctorRepository, patientRepository, validator);
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private Doctor buildDoctor(int id) {
        Doctor d = new Doctor();
        d.setId(id);
        return d;
    }

    private Patient buildPatient(int id) {
        Patient p = new Patient();
        p.setId(id);
        return p;
    }

    private Appointment buildAppointment(int id, Doctor doctor, Patient patient) {
        Appointment a = new Appointment();
        a.setAppointmentId(id);
        a.setDoctor(doctor);
        a.setPatient(patient);
        a.setDate(LocalDate.of(2025, 6, 2));
        a.setStartTime(LocalTime.of(8, 0));
        a.setEndTime(LocalTime.of(8, 30));
        return a;
    }

    // -----------------------------------------------------------------------
    // scheduleAppointment
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Agendar cita válida -> guarda con estado AGENDADA y retorna true")
    void scheduleAppointment_datosValidos_guardaYRetornaTrue() {
        Doctor doctor = buildDoctor(1);
        Patient patient = buildPatient(2);
        Appointment appointment = buildAppointment(0, doctor, patient);

        when(doctorRepository.findById(1)).thenReturn(doctor);
        when(patientRepository.findById(2)).thenReturn(patient);
        when(appointmentRepository.findByDoctorAndDate(1, appointment.getDate().toString()))
                .thenReturn(Collections.emptyList());
        when(appointmentRepository.save(appointment)).thenReturn(true);

        boolean result = service.scheduleAppointment(appointment);

        assertTrue(result);
        assertEquals(AppointmentStatus.AGENDADA, appointment.getStatus());
        verify(appointmentRepository).save(appointment);
    }

    @Test
    @DisplayName("Validador lanza excepción por conflicto de horario -> no se guarda la cita")
    void scheduleAppointment_conflictoHorario_lanzaExcepcion() {
        Doctor doctor = buildDoctor(1);
        Patient patient = buildPatient(2);
        Appointment appointment = buildAppointment(0, doctor, patient);

        when(doctorRepository.findById(1)).thenReturn(doctor);
        when(patientRepository.findById(2)).thenReturn(patient);
        when(appointmentRepository.findByDoctorAndDate(1, appointment.getDate().toString()))
                .thenReturn(List.of(appointment));

        // El validador detecta conflicto y lanza excepción
        doThrow(new IllegalArgumentException("Conflicto de horario"))
                .when(validator).validate(any(), any(), any(), any());

        assertThrows(IllegalArgumentException.class, () -> service.scheduleAppointment(appointment));
        verify(appointmentRepository, never()).save(any());
    }

    // -----------------------------------------------------------------------
    // rescheduleAppointment
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Reagendar cita existente -> actualiza con estado REAGENDADA y retorna true")
    void rescheduleAppointment_citaExistente_actualizaYRetornaTrue() {
        Doctor doctor = buildDoctor(1);
        Patient patient = buildPatient(2);
        Appointment appointment = buildAppointment(5, doctor, patient);

        when(doctorRepository.findById(1)).thenReturn(doctor);
        when(patientRepository.findById(2)).thenReturn(patient);
        when(appointmentRepository.findByDoctorAndDate(1, appointment.getDate().toString()))
                .thenReturn(Collections.emptyList());
        when(appointmentRepository.update(appointment)).thenReturn(true);

        boolean result = service.rescheduleAppointment(appointment);

        assertTrue(result);
        assertEquals(AppointmentStatus.REAGENDADA, appointment.getStatus());
        verify(appointmentRepository).update(appointment);
    }

    @Test
    @DisplayName("Reagendar cita null -> validador lanza excepción")
    void rescheduleAppointment_citaNula_lanzaExcepcion() {
        doThrow(new IllegalArgumentException("La cita no existe"))
                .when(validator).validateExists(null);

        assertThrows(IllegalArgumentException.class, () -> service.rescheduleAppointment(null));
    }

    // -----------------------------------------------------------------------
    // cancelAppointment
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Cancelar cita existente -> cambia estado a CANCELADA y retorna true")
    void cancelAppointment_citaExistente_cambiaEstadoYRetornaTrue() {
        Doctor doctor = buildDoctor(1);
        Patient patient = buildPatient(2);
        Appointment appointment = buildAppointment(10, doctor, patient);
        appointment.setStatus(AppointmentStatus.AGENDADA);

        when(appointmentRepository.findById(10)).thenReturn(appointment);
        when(appointmentRepository.update(appointment)).thenReturn(true);

        boolean result = service.cancelAppointment(10);

        assertTrue(result);
        assertEquals(AppointmentStatus.CANCELADA, appointment.getStatus());
    }

    @Test
    @DisplayName("Cancelar cita inexistente -> validador lanza excepción")
    void cancelAppointment_citaNoExistente_lanzaExcepcion() {
        when(appointmentRepository.findById(99)).thenReturn(null);
        doThrow(new IllegalArgumentException("Cita no encontrada"))
                .when(validator).validateExists(null);

        assertThrows(IllegalArgumentException.class, () -> service.cancelAppointment(99));
    }

    // -----------------------------------------------------------------------
    // markAsAttended
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Marcar como atendida -> cambia estado a ATENDIDA y retorna true")
    void markAsAttended_citaExistente_cambiaEstadoAAtendida() {
        Doctor doctor = buildDoctor(1);
        Patient patient = buildPatient(2);
        Appointment appointment = buildAppointment(7, doctor, patient);
        appointment.setStatus(AppointmentStatus.AGENDADA);

        when(appointmentRepository.findById(7)).thenReturn(appointment);
        when(appointmentRepository.update(appointment)).thenReturn(true);

        boolean result = service.markAsAttended(7);

        assertTrue(result);
        assertEquals(AppointmentStatus.ATENDIDA, appointment.getStatus());
    }

    // -----------------------------------------------------------------------
    // findAppointment
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Buscar cita existente -> retorna la cita correcta")
    void findAppointment_citaExistente_retornaCita() {
        Doctor doctor = buildDoctor(1);
        Patient patient = buildPatient(2);
        Appointment appointment = buildAppointment(3, doctor, patient);

        when(appointmentRepository.findById(3)).thenReturn(appointment);

        Appointment result = service.findAppointment(3);

        assertNotNull(result);
        assertEquals(3, result.getAppointmentId());
    }

    // -----------------------------------------------------------------------
    // listAppointments
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Listar todas las citas -> retorna la lista del repositorio")
    void listAppointments_retornaListaDelRepositorio() {
        Doctor doctor = buildDoctor(1);
        Patient patient = buildPatient(2);
        List<Appointment> citas = List.of(
                buildAppointment(1, doctor, patient),
                buildAppointment(2, doctor, patient)
        );
        when(appointmentRepository.findAll()).thenReturn(citas);

        List<Appointment> result = service.listAppointments();

        assertEquals(2, result.size());
    }
}