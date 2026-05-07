package co.unicauca.piedrazul.domain.services;

import co.unicauca.piedrazul.domain.access.IDoctorRepository;
import co.unicauca.piedrazul.domain.entities.Doctor;
import co.unicauca.piedrazul.domain.services.interfaces.IDoctorService;
import co.unicauca.piedrazul.domain.services.interfaces.IDoctorValidator;
import java.util.List;

/**
 * @author Valentina Añasco 
 * @author Camila Dorado
 * @author Felipe Gutierrez
 * @author Ginner Ortega
 * @author Santiago Solarte 
 */

public class DoctorService implements IDoctorService {
    
    private final IDoctorRepository doctorRepository;
    private final IDoctorValidator doctorValidator; // Dependemos de la interfaz

    public DoctorService(IDoctorRepository doctorRepository, IDoctorValidator doctorValidator) {
        this.doctorRepository = doctorRepository;
        this.doctorValidator = doctorValidator;
    }

    @Override
    public boolean registerDoctor(Doctor doctor) {
   
        doctorValidator.validateDoctor(doctor);
        
        return doctorRepository.save(doctor);
    }
    @Override
   public Doctor findDoctor(int id) {
        Doctor doctor = doctorRepository.findById(id);
        doctorValidator.validateDoctor(doctor);
        return doctor;
    }

    @Override
    public List<Doctor> listActiveDoctors() {
        List<Doctor> doctors = doctorRepository.findAllActive();
        doctorValidator.validateListNotEmpty(doctors); 
        return doctors;
    }

    @Override
    public boolean modifyDoctor(Doctor doctor) {
        // Primero verificamos que exista
        Doctor existing = doctorRepository.findById(doctor.getId());
        doctorValidator.validateExists(existing);
        
        // Validamos que los nuevos datos sean correctos
        doctorValidator.validateDoctor(doctor); 
        
        return doctorRepository.update(doctor);
    }

    @Override
    public boolean deactivateDoctor(int id) {
        Doctor doctor = doctorRepository.findById(id);
        doctorValidator.validateExists(doctor);
        
        return doctorRepository.deactivate(id);
    }
}
