package co.unicauca.piedrazul.domain.services.validators;


import co.unicauca.piedrazul.domain.entities.Patient;
import co.unicauca.piedrazul.domain.services.interfaces.IPatientValidator;

public class PatientValidator extends UserValidator implements IPatientValidator {

    @Override
    public void validatePatient(Patient patient) {
        //metodo de la clase padre
        super.validateUser(patient); 
        
        //validamos datos especificos
        validatePhone(patient.getPhone());
        validateGender(patient.getGender());
    }

    
    private void validatePhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            throw new IllegalArgumentException("El teléfono es obligatorio");
        }
    }

    private void validateGender(String gender) {
        if (gender == null || gender.trim().isEmpty()) {
            throw new IllegalArgumentException("El género es obligatorio");
        }
    }

    @Override
    public void validateExists(Patient patient) {
        if(patient == null)
            throw new IllegalArgumentException("Paciente no encontrado");
    }
}