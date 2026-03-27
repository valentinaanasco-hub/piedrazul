package co.unicauca.piedrazul.presentation.controllers;

import co.unicauca.piedrazul.domain.entities.Appointment;
import co.unicauca.piedrazul.domain.entities.Doctor;
import co.unicauca.piedrazul.domain.entities.Patient;
import co.unicauca.piedrazul.domain.entities.SystemParameter;
import co.unicauca.piedrazul.domain.entities.enums.AppointmentStatus;
import co.unicauca.piedrazul.domain.services.ManualAppointmentService;
import co.unicauca.piedrazul.domain.services.AvailabilityService;
import co.unicauca.piedrazul.domain.services.DoctorService;
import co.unicauca.piedrazul.domain.services.PatientService;
import co.unicauca.piedrazul.domain.services.SystemParameterService;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Valentina Añasco
 * @author Camila Dorado
 * @author Felipe Gutierrez
 * @author Ginner Ortega
 * @author Santiago Solarte
 */
public class RegisterAppointmentController {

    private final ManualAppointmentService appointmentService;
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

    // Último mensaje de error para la vista
    private String lastErrorMessage;

    public RegisterAppointmentController(
            ManualAppointmentService appointmentService,
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

    // Retorna médicos activos; lista vacía si no hay registros
    public List<Doctor> loadActiveDoctors() {
        try {
            lastErrorMessage = null;
            return doctorService.listActiveDoctors();
        } catch (IllegalArgumentException e) {
            lastErrorMessage = e.getMessage();
            return new ArrayList<>();
        }
    }

    // Fecha mínima para el DatePicker; hoy si no hay parámetro configurado
    public LocalDate getStartDate() {
        try {
            SystemParameter param = parameterService.findParameter("start_date_schedule");
            if (param != null && param.getValue() != null) {
                return LocalDate.parse(param.getValue());
            }
        } catch (Exception e) {
            lastErrorMessage = e.getMessage();
        }
        return LocalDate.now();
    }

    // Fecha máxima para el DatePicker; un mes adelante si no hay parámetro
    public LocalDate getEndDate() {
        try {
            SystemParameter param = parameterService.findParameter("end_date_schedule");
            if (param != null && param.getValue() != null) {
                return LocalDate.parse(param.getValue());
            }
        } catch (Exception e) {
            lastErrorMessage = e.getMessage();
        }
        return LocalDate.now().plusMonths(1);
    }

    // Actualiza el médico seleccionado y recalcula horarios disponibles
    public void onDoctorSelected(Doctor doctor) {
        this.selectedDoctor = doctor;
        this.selectedSlot = null;
        refreshAvailableSlots();
    }

    // Actualiza la fecha seleccionada y recalcula horarios disponibles
    public void onDateSelected(LocalDate date) {
        this.selectedDate = date;
        this.selectedSlot = null;
        refreshAvailableSlots();
    }

    private void refreshAvailableSlots() {
        if (selectedDoctor == null || selectedDate == null) {
            return;
        }
        try {
            availableSlots = availabilityService.getAvailableSlots(
                    selectedDoctor.getId(),
                    selectedDate
            );
        } catch (Exception e) {
            lastErrorMessage = e.getMessage();
            availableSlots = new ArrayList<>();
        }
    }

    public void onSlotSelected(LocalTime slot) {
        this.selectedSlot = slot;
    }

    // Busca un paciente por documento; null si no existe o hay error
    public Patient findPatientById(int id) {
        try {
            lastErrorMessage = null;
            return patientService.findPatient(id);
        } catch (IllegalArgumentException e) {
            lastErrorMessage = e.getMessage();
            return null;
        }
    }

    public void onPatientSelected(Patient patient) {
        this.selectedPatient = patient;
    }

    // Registra la cita con los datos seleccionados en el formulario
    public boolean registerAppointment(String loggedUserRole) {
        try {
            lastErrorMessage = null;

            if (selectedDoctor == null || selectedPatient == null
                    || selectedDate == null || selectedSlot == null) {
                throw new IllegalArgumentException("Complete todos los campos obligatorios.");
            }

            int interval = availabilityService.getIntervalMinutesForDoctorOnDate(
                    selectedDoctor.getId(),
                    selectedDate
            );

            LocalTime endTime = selectedSlot.plusMinutes(interval);

            Appointment appointment = new Appointment();
            appointment.setDoctor(selectedDoctor);
            appointment.setPatient(selectedPatient);
            appointment.setDate(selectedDate);
            appointment.setStartTime(selectedSlot);
            appointment.setEndTime(endTime);
            appointment.setStatus(AppointmentStatus.AGENDADA);

            return appointmentService.scheduleAppointment(appointment);

        } catch (IllegalArgumentException | IllegalStateException e) {
            lastErrorMessage = e.getMessage();
            throw e; // la vista muestra el mensaje en un Alert
        } catch (Exception e) {
            lastErrorMessage = e.getMessage();
            return false;
        }
    }

    // Reinicia el estado del formulario
    public void reset() {
        selectedDoctor = null;
        selectedPatient = null;
        selectedDate = null;
        selectedSlot = null;
        availableSlots = new ArrayList<>();
        lastErrorMessage = null;
    }

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

    public String getLastErrorMessage() {
        return lastErrorMessage;
    }
}