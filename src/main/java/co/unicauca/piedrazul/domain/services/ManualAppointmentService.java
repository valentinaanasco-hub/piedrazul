package co.unicauca.piedrazul.domain.services;

import co.unicauca.piedrazul.domain.access.IAppointmentRepository;
import co.unicauca.piedrazul.domain.access.IDoctorRepository;
import co.unicauca.piedrazul.domain.access.IPatientRepository;
import co.unicauca.piedrazul.domain.entities.Appointment;
import co.unicauca.piedrazul.domain.entities.Doctor;
import co.unicauca.piedrazul.domain.entities.Patient;
import co.unicauca.piedrazul.domain.entities.enums.AppointmentStatus;
import co.unicauca.piedrazul.domain.services.interfaces.IAppointmentService;
import java.util.List;
import co.unicauca.piedrazul.domain.services.interfaces.IManualAppointmentValidator;

public class ManualAppointmentService implements IAppointmentService{

    private final IAppointmentRepository appointmentRepository;
    private final IDoctorRepository doctorRepository;
    private final IPatientRepository patientRepository;
    private final IManualAppointmentValidator validator;

    // Inyección de dependencias 
    public ManualAppointmentService(IAppointmentRepository appointmentRepository,
            IDoctorRepository doctorRepository,
            IPatientRepository patientRepository,
            IManualAppointmentValidator validator) {
        this.appointmentRepository = appointmentRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.validator = validator;
    }

    @Override
    public boolean scheduleAppointment(Appointment appointment) {
        // Recuperación de información 
        Doctor doctor = doctorRepository.findById(appointment.getDoctor().getId());
        Patient patient = patientRepository.findById(appointment.getPatient().getId());
        List<Appointment> existing = appointmentRepository
                .findByDoctorAndDate(appointment.getDoctor().getId(),
                        appointment.getDate().toString());

        // El validador asegura que no existan conflictos de horario o datos nulos
        validator.validate(appointment, doctor, patient, existing);

        appointment.setStatus(AppointmentStatus.AGENDADA);
        return appointmentRepository.save(appointment);
    }

    @Override
    public boolean rescheduleAppointment(Appointment appointment) {
        // Verificación de existencia previa antes de procesar cambios
        validator.validateExists(appointment);
        Doctor doctor = doctorRepository.findById(appointment.getDoctor().getId());
        Patient patient = patientRepository.findById(appointment.getPatient().getId());
        List<Appointment> existing = appointmentRepository.findByDoctorAndDate(appointment.getDoctor().getId(),
                appointment.getDate().toString());

        // Reutilización de lógica de validación para asegurar consistencia en el cambio
        validator.validate(appointment, doctor, patient, existing);

        appointment.setStatus(AppointmentStatus.REAGENDADA);
        return appointmentRepository.update(appointment);
    }

    @Override
    public boolean cancelAppointment(int id) {
        // Localización de la cita y transición de estado a CANCELADA
        Appointment appointment = findAppointment(id);
        appointment.setStatus(AppointmentStatus.CANCELADA);
        return appointmentRepository.update(appointment);
    }

    @Override
    public boolean markAsAttended(int id) {
        // Actualización de estado tras la ejecución exitosa de la consulta
        Appointment appointment = findAppointment(id);
        appointment.setStatus(AppointmentStatus.ATENDIDA);
        return appointmentRepository.update(appointment);
    }

    @Override
    public Appointment findAppointment(int id) {
        // Búsqueda única con validación de existencia inmediata
        Appointment appointment = appointmentRepository.findById(id);
        validator.validateExists(appointment);
        return appointment;
    }

    @Override
    public List<Appointment> listAppointments() {
        return appointmentRepository.findAll();
    }
}
