package co.unicauca.piedrazul.domain.services.validators;

import co.unicauca.piedrazul.domain.entities.Doctor;
import co.unicauca.piedrazul.domain.entities.User;
import co.unicauca.piedrazul.domain.services.interfaces.IDoctorValidator;
import java.util.List;

/**
 * @author Valentina Añasco 
 * @author Camila Dorado
 * @author Felipe Gutierrez
 * @author Ginner Ortega
 * @author Santiago Solarte 
 */
                                                                 //OP (OPEN - CLOSE)
public class DoctorValidator extends UserValidator implements IDoctorValidator { //Aplicamos principio LSP (Liskov Substitution Principle)
    
    @Override
    public void validateDoctor(Doctor doctor) {
        // Validamos el usuario 
        super.validateUser((User)doctor);

        // Validamos lo específico del Médico
        validateProfessionalId(doctor.getProfessionalId());
    }

    private void validateProfessionalId(String professionalId) {
        if (professionalId == null || professionalId.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID profesional es obligatorio para el médico");
        }
        
        if (professionalId.length() < 3 && professionalId.length() > 11 ) {
            throw new IllegalArgumentException("El ID profesional no es válido");
        }
    }
    
    @Override
    public void validateExists(Doctor doctor) {
        if (doctor == null) {
            throw new IllegalArgumentException("Médico no encontrado");
        }
    }
    @Override
    public void validateListNotEmpty(List<Doctor> doctors) {
        if (doctors == null || doctors.isEmpty()) {
            throw new IllegalArgumentException("No hay registros de médicos");
        }
    }
}