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
 *
 * Cubre los 4 escenarios de scheduleAppointment, listByDoctorAndDate,
 * reagendamiento, cancelación y marcado como atendida.
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
        d.setFirstName("Carlos");
        d.setFirstSurname("Lopez");
        return d;
    }

    private Patient buildPatient(int id) {
        Patient p = new Patient();
        p.setId(id);
        p.setFirstName("Ana");
        p.setFirstSurname("Gomez");
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
    // scheduleAppointment — Test 1: Registro exitoso
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("scheduleAppointment: todos los datos válidos -> guarda con estado AGENDADA")
    void scheduleAppointment_datosValidos_guardaConEstadoAgendada() {
        // GIVEN
        Doctor doctor = buildDoctor(1);
        Patient patient = buildPatient(2);
        Appointment appointment = buildAppointment(0, doctor, patient);

        when(doctorRepository.findById(1)).thenReturn(doctor);
        when(patientRepository.findById(2)).thenReturn(patient);
        when(appointmentRepository.findByDoctorAndDate(1, appointment.getDate().toString()))
                .thenReturn(Collections.emptyList());
        when(appointmentRepository.save(appointment)).thenReturn(true);

        // WHEN
        boolean result = service.scheduleAppointment(appointment);

        // THEN
        assertTrue(result);
        assertEquals(AppointmentStatus.AGENDADA, appointment.getStatus(),
                "El estado debe cambiar a AGENDADA antes de guardar");
        verify(validator).validate(appointment, doctor, patient, Collections.emptyList());
        verify(appointmentRepository).save(appointment);
    }

    // -----------------------------------------------------------------------
    // scheduleAppointment — Test 2: Patient no existe → validador lanza excepción
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("scheduleAppointment: Patient no existe (null) -> validator lanza excepción y no se guarda")
    void scheduleAppointment_patientNoExiste_lanzaExcepcion() {
        // GIVEN
        Doctor doctor = buildDoctor(1);
        Patient patient = buildPatient(99);
        Appointment appointment = buildAppointment(0, doctor, patient);

        when(doctorRepository.findById(1)).thenReturn(doctor);
        when(patientRepository.findById(99)).thenReturn(null); // Paciente no existe
        when(appointmentRepository.findByDoctorAndDate(1, appointment.getDate().toString()))
                .thenReturn(Collections.emptyList());

        // El validador detecta patient == null y lanza excepción
        doThrow(new IllegalArgumentException("El paciente no existe"))
                .when(validator).validate(appointment, doctor, null, Collections.emptyList());

        // WHEN / THEN
        assertThrows(IllegalArgumentException.class, () -> service.scheduleAppointment(appointment));
        verify(appointmentRepository, never()).save(any());
    }

    // -----------------------------------------------------------------------
    // scheduleAppointment — Test 3: Doctor no existe → validador lanza excepción
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("scheduleAppointment: Doctor no existe (null) -> validator lanza excepción y no se guarda")
    void scheduleAppointment_doctorNoExiste_lanzaExcepcion() {
        // GIVEN
        Doctor doctor = buildDoctor(99);
        Patient patient = buildPatient(2);
        Appointment appointment = buildAppointment(0, doctor, patient);

        when(doctorRepository.findById(99)).thenReturn(null); // Doctor no existe
        when(patientRepository.findById(2)).thenReturn(patient);
        when(appointmentRepository.findByDoctorAndDate(99, appointment.getDate().toString()))
                .thenReturn(Collections.emptyList());

        doThrow(new IllegalArgumentException("El médico no existe"))
                .when(validator).validate(appointment, null, patient, Collections.emptyList());

        // WHEN / THEN
        assertThrows(IllegalArgumentException.class, () -> service.scheduleAppointment(appointment));
        verify(appointmentRepository, never()).save(any());
    }

    // -----------------------------------------------------------------------
    // scheduleAppointment — Test 4: Conflicto de horario → no se guarda
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("scheduleAppointment: conflicto de horario en fecha/hora -> validator rechaza y no guarda")
    void scheduleAppointment_conflictoHorario_noGuarda() {
        // GIVEN
        Doctor doctor = buildDoctor(1);
        Patient patient = buildPatient(2);
        Appointment appointment = buildAppointment(0, doctor, patient);

        // Ya existe una cita en ese horario
        Appointment citaExistente = buildAppointment(5, doctor, patient);
        List<Appointment> citasDelDia = List.of(citaExistente);

        when(doctorRepository.findById(1)).thenReturn(doctor);
        when(patientRepository.findById(2)).thenReturn(patient);
        when(appointmentRepository.findByDoctorAndDate(1, appointment.getDate().toString()))
                .thenReturn(citasDelDia);

        doThrow(new IllegalArgumentException("Conflicto de horario: ese slot ya está ocupado"))
                .when(validator).validate(appointment, doctor, patient, citasDelDia);

        // WHEN / THEN
        assertThrows(IllegalArgumentException.class, () -> service.scheduleAppointment(appointment));
        verify(appointmentRepository, never()).save(any());
    }

    // -----------------------------------------------------------------------
    // listAppointments — Test 5: lista filtrada correctamente
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("listAppointments: retorna la lista completa del repositorio")
    void listAppointments_retornaListaDelRepositorio() {
        // GIVEN
        Doctor doctor = buildDoctor(1);
        Patient patient = buildPatient(2);
        List<Appointment> citas = List.of(
                buildAppointment(1, doctor, patient),
                buildAppointment(2, doctor, patient),
                buildAppointment(3, doctor, patient)
        );
        when(appointmentRepository.findAll()).thenReturn(citas);

        // WHEN
        List<Appointment> result = service.listAppointments();

        // THEN
        assertEquals(3, result.size(), "Debe retornar exactamente 3 citas");
        verify(appointmentRepository).findAll();
    }

    // -----------------------------------------------------------------------
    // listAppointments — Test 6: doctor sin citas ese día → lista vacía sin NPE
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("listAppointments: repositorio retorna lista vacía -> no lanza NullPointerException")
    void listAppointments_sinCitas_retornaListaVaciaSinExcepcion() {
        // GIVEN
        when(appointmentRepository.findAll()).thenReturn(Collections.emptyList());

        // WHEN / THEN: no debe lanzar excepción
        List<Appointment> result = assertDoesNotThrow(
                () -> service.listAppointments(),
                "No debe lanzar NullPointerException cuando no hay citas"
        );
        assertTrue(result.isEmpty());
    }

    // -----------------------------------------------------------------------
    // rescheduleAppointment — Test 7: reagendar cita existente
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("rescheduleAppointment: cita válida -> actualiza con estado REAGENDADA")
    void rescheduleAppointment_citaExistente_actualizaConEstadoReagendada() {
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

    // -----------------------------------------------------------------------
    // cancelAppointment — Test 8: cambio de estado a CANCELADA
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("cancelAppointment: cita existente -> cambia estado a CANCELADA y retorna true")
    void cancelAppointment_citaExistente_cambiaEstadoACancelada() {
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

    // -----------------------------------------------------------------------
    // markAsAttended — Test 9: cambio de estado a ATENDIDA
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("markAsAttended: cita existente -> cambia estado a ATENDIDA y retorna true")
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
    // findAppointment — Test 10: cita no existe → excepción
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("findAppointment: cita inexistente -> validator lanza excepción")
    void findAppointment_noExiste_lanzaExcepcion() {
        when(appointmentRepository.findById(999)).thenReturn(null);
        doThrow(new IllegalArgumentException("Cita no encontrada"))
                .when(validator).validateExists(null);

        assertThrows(IllegalArgumentException.class, () -> service.findAppointment(999));
    }
}