package co.unicauca.piedrazul.domain.services;

import co.unicauca.piedrazul.domain.access.ISpecialtyRepository;
import co.unicauca.piedrazul.domain.entities.Specialty;
import co.unicauca.piedrazul.domain.services.interfaces.ISpecialtyService;
import co.unicauca.piedrazul.domain.services.interfaces.ISpecialtyValidator;
import java.util.List;

/**
 * @author Valentina Añasco
 * @author Camila Dorado
 * @author Felipe Gutierrez
 * @author Ginner Ortega
 * @author Santiago Solarte
 */
public class SpecialtyService implements ISpecialtyService {

    private final ISpecialtyRepository specialtyRepository;
    private final ISpecialtyValidator validator; // Inyectamos el validador

    public SpecialtyService(ISpecialtyRepository specialtyRepository, ISpecialtyValidator validator) {
        this.specialtyRepository = specialtyRepository;
        this.validator = validator;
    }

    @Override
    public Specialty findByName(String name) {
        Specialty specialty = specialtyRepository.findByName(name);
        validator.validateExists(specialty);
        return specialty;
    }

    @Override
    public Specialty findSpecialty(int id) {
        Specialty specialty = specialtyRepository.findById(id);
        validator.validateExists(specialty);
        return specialty;
    }

    @Override
    public List<Specialty> listSpecialties() {
        List<Specialty> specialties = specialtyRepository.findAll();
        validator.validateListNotEmpty(specialties);
        return specialties;
    }

    @Override
    public boolean assignSpecialtyToDoctor(int doctorId, int specialtyId) {
        Specialty specialty = specialtyRepository.findById(specialtyId);
        validator.validateExists(specialty); // Reutilizamos validación
        return specialtyRepository.assignSpecialtyToDoctor(doctorId, specialtyId);
    }
    @Override
    public List<Specialty> findByDoctorId(int doctorId) {
        return specialtyRepository.findSpecialtiesByDoctorId(doctorId);
    }
}
