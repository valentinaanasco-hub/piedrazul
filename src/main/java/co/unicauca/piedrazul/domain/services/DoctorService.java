package co.unicauca.piedrazul.domain.services;

import co.unicauca.piedrazul.domain.access.IDoctorRepository;
import co.unicauca.piedrazul.domain.access.IDoctorScheduleRepository;
import co.unicauca.piedrazul.domain.access.ISpecialtyRepository;
import co.unicauca.piedrazul.domain.entities.Doctor;
import java.util.List;

/**
 * @author Valentina Añasco 
 * @author Camila Dorado
 * @author Felipe Gutierrez
 * @author Ginner Ortega
 * @author Santiago Solarte 
 */

public class DoctorService {
    
    private final IDoctorRepository doctorRepository;

    private final IDoctorScheduleRepository doctorSheduleRepository;
    private final ISpecialtyRepository specialtyRepository;

    public DoctorService(IDoctorRepository doctorRepository,  IDoctorScheduleRepository doctorSheduleRepository
                        , ISpecialtyRepository specialtyRepository) {
        this.doctorRepository = doctorRepository;
        this.doctorSheduleRepository = doctorSheduleRepository;
        this.specialtyRepository = specialtyRepository;

    }

    public boolean registerDoctor(Doctor doctor) {
        // Valida campos obligatorios del médico
        if (doctor.getFirstName() == null || doctor.getFirstName().trim().isEmpty())
            throw new IllegalArgumentException("El nombre del médico es obligatorio");
        if (doctor.getProfessionalId() == null || doctor.getProfessionalId().trim().isEmpty())
            throw new IllegalArgumentException("El id profesional es obligatorio");
        if (doctor.getUsername() == null || doctor.getUsername().trim().isEmpty())
            throw new IllegalArgumentException("El username es obligatorio");

        return doctorRepository.save(doctor);
    }

    public Doctor findDoctor(int id) {
        Doctor doctor = doctorRepository.findById(id);
        if (doctor == null){
            throw new IllegalArgumentException("Médico no encontrado");
        }
        doctor.setSchedules(doctorSheduleRepository.findByDoctorId(doctor.getId()));
        doctor.setSpecialties(specialtyRepository.findByDoctorId(doctor.getId()));

        return doctor;
    }

    public List<Doctor> listActiveDoctors() {
        
        List<Doctor> doctors = doctorRepository.findAllActive();
        if(doctors.isEmpty()){
            throw new IllegalArgumentException("No hay registros de medicos");
        }
        for (Doctor doctor : doctors){
            doctor.setSchedules(doctorSheduleRepository.findByDoctorId(doctor.getId()));
            doctor.setSpecialties(specialtyRepository.findByDoctorId(doctor.getId()));
            
        }
        return doctors;
    }

    public boolean modifyDoctor(Doctor doctor) {
        if (doctorRepository.findById(doctor.getId()) == null)
            throw new IllegalArgumentException("Médico no encontrado");
        return doctorRepository.update(doctor);
    }

    public boolean deactivateDoctor(int id) {
        // Desactiva en lugar de eliminar para conservar historial de citas
        if (doctorRepository.findById(id) == null)
            throw new IllegalArgumentException("Médico no encontrado");
        return doctorRepository.desactivate(id);
    }
}
