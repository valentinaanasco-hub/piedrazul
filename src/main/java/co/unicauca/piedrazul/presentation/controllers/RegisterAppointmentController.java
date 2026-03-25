package co.unicauca.piedrazul.presentation.controllers;

import co.unicauca.piedrazul.domain.entities.Appointment;
import co.unicauca.piedrazul.domain.entities.Doctor;
import co.unicauca.piedrazul.domain.entities.Patient;
import co.unicauca.piedrazul.domain.entities.SystemParameter;
import co.unicauca.piedrazul.domain.services.AppointmentService;
import co.unicauca.piedrazul.domain.services.AvailabilityService;
import co.unicauca.piedrazul.domain.services.DoctorService;
import co.unicauca.piedrazul.domain.services.PatientService;
import co.unicauca.piedrazul.domain.services.SystemParameterService;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class RegisterAppointmentController {

    // Servicios (DIP)
    private final AppointmentService appointmentService;
    private final DoctorService doctorService;
    private final AvailabilityService availabilityService;
    private final PatientService patientService;
    private final SystemParameterService parameterService;

    // Estado del formulario
    private Doctor selectedDoctor;
    private Patient selectedPatient;
    private LocalDate selectedDate;
    private LocalTime selectedSlot;
    private List<LocalTime> availableSlots = new ArrayList<>();

    // El controlador RECIBE sus dependencias ya construidas
    public RegisterAppointmentController(
            AppointmentService appointmentService,
            DoctorService doctorService,
            AvailabilityService availabilityService,
            PatientService patientService,
            SystemParameterService parameterService) {

        this.appointmentService = appointmentService;
        this.doctorService = doctorService;
        this.availabilityService = availabilityService;
        this.patientService = patientService;
        this.parameterService = parameterService;
    }

    // ───────────── DATOS INICIALES ─────────────
    public List<Doctor> loadActiveDoctors() {
        return doctorService.listActiveDoctors();
    }

    // Fecha mínima para el DatePicker
    public LocalDate getStartDate() {
        try {
            SystemParameter param = parameterService.findParameter("start_date_schedule");
            if (param != null && param.getValue() != null) {
                return LocalDate.parse(param.getValue());
            }
        } catch (Exception e) {
            System.err.println("Error leyendo start_date_schedule");
        }
        return LocalDate.now();
    }

    // Fecha máxima para el DatePicker
    public LocalDate getEndDate() {
        try {
            SystemParameter param = parameterService.findParameter("end_date_schedule");
            if (param != null && param.getValue() != null) {
                return LocalDate.parse(param.getValue());
            }
        } catch (Exception e) {
            System.err.println("Error leyendo end_date_schedule");
        }
        return LocalDate.now().plusMonths(1);
    }

    // ───────────── EVENTOS UI ─────────────
    public void onDoctorSelected(Doctor doctor) {
        this.selectedDoctor = doctor;
        this.selectedSlot = null;
        refreshAvailableSlots();
    }

    public void onDateSelected(LocalDate date) {
        this.selectedDate = date;
        this.selectedSlot = null;
        refreshAvailableSlots();
    }

    private void refreshAvailableSlots() {
        if (selectedDoctor == null || selectedDate == null) {
            return;
        }

        availableSlots = availabilityService.getAvailableSlots(
                selectedDoctor.getId(),
                selectedDate
        );
    }

    public void onSlotSelected(LocalTime slot) {
        this.selectedSlot = slot;
    }

    // ───────────── PACIENTE ─────────────
    public Patient findPatientById(int id) {
        try {
            return patientService.findPatient(id);
        } catch (Exception e) {
            return null;
        }
    }

    public void onPatientSelected(Patient patient) {
        this.selectedPatient = patient;
    }

    // ───────────── REGISTRAR CITA ─────────────
    public boolean registerAppointment(String loggedUserRole) {

        /*
        if (!loggedUserRole.equals("AGENDADOR") &&
            !loggedUserRole.equals("DOCTOR")) {
            throw new IllegalArgumentException("No tiene permisos");
        }
        /*
        
         */
        if (selectedDoctor == null || selectedPatient == null
                || selectedDate == null || selectedSlot == null) {
            throw new IllegalArgumentException("Faltan datos");
        }

        // calcular hora fin
        int interval = availabilityService.getIntervalMinutesForDoctorOnDate(
                selectedDoctor.getId(),
                selectedDate
        );

        LocalTime endTime = selectedSlot.plusMinutes(interval);

        // crear cita
        Appointment appointment = new Appointment();
        appointment.setDoctor(selectedDoctor);
        appointment.setPatient(selectedPatient);
        appointment.setDate(selectedDate);
        appointment.setStartTime(selectedSlot);
        appointment.setEndTime(endTime);
        appointment.setStatus("AGENDADA");

        return appointmentService.scheduleAppointment(appointment);
    }

    // ───────────── RESET ─────────────
    public void reset() {
        selectedDoctor = null;
        selectedPatient = null;
        selectedDate = null;
        selectedSlot = null;
        availableSlots = new ArrayList<>();
    }

    // ───────────── GETTERS ─────────────
    public List<LocalTime> getAvailableSlots() {
        return availableSlots;
    }

    public Doctor getSelectedDoctor() {
        return selectedDoctor;
    }

    public Patient getSelectedPatient() {
        return selectedPatient;
    }

    public LocalDate getSelectedDate() {
        return selectedDate;
    }

    public LocalTime getSelectedSlot() {
        return selectedSlot;
    }
}
