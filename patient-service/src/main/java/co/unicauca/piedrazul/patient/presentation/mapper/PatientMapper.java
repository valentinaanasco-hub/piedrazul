package co.unicauca.piedrazul.patient.presentation.mapper;

import co.unicauca.piedrazul.patient.presentation.dto.PatientDTOs;
import org.springframework.stereotype.Component;

import co.unicauca.piedrazul.patient.domain.entities.Patient;
import co.unicauca.piedrazul.patient.domain.enums.UserState;
import co.unicauca.piedrazul.patient.presentation.dto.PatientDTOs.AgendadorRegisterRequest;
import co.unicauca.piedrazul.patient.presentation.dto.PatientDTOs.PatientResponse;
import co.unicauca.piedrazul.patient.presentation.dto.PatientDTOs.WebRegisterRequest;

/**
 * Mapper que convierte entre entidades Patient y DTOs.
 * Patrón estructural: evita exponer la entidad directamente en la API.
 *
 * @author Santiago Solarte
 */
@Component
public class PatientMapper {

    // --- Request web → entidad ---
    public Patient toEntity(WebRegisterRequest request) {
        Patient patient = new Patient();
        patient.setId(request.documentId());
        patient.setUserTypeId(request.userTypeId());
        patient.setFirstName(request.firstName());
        patient.setMiddleName(request.middleName());
        patient.setFirstSurname(request.firstSurname());
        patient.setLastName(request.lastName());
        patient.setEmail(request.email());
        patient.setUsername(request.email());
        patient.setPassword(request.password());
        patient.setPhone(request.phone());
        patient.setGender(request.gender());
        patient.setBirthDay(request.birthDay());
        patient.setBirthMonth(request.birthMonth());
        patient.setBirthYear(request.birthYear());
        patient.setState(UserState.ACTIVO);
        return patient;
    }

    // --- Request agendador → entidad ---
    public Patient toEntity(AgendadorRegisterRequest request) {
        Patient patient = new Patient();
        patient.setId(request.documentId());
        patient.setUserTypeId(request.userTypeId());
        patient.setFirstName(request.firstName());
        patient.setMiddleName(request.middleName());
        patient.setFirstSurname(request.firstSurname());
        patient.setLastName(request.lastName());
        patient.setEmail(request.email());
        patient.setPhone(request.phone());
        patient.setGender(request.gender());
        patient.setBirthDay(request.birthDay());
        patient.setBirthMonth(request.birthMonth());
        patient.setBirthYear(request.birthYear());
        patient.setState(UserState.ACTIVO);
        return patient;
    }

    // Entidad -> request
    public PatientDTOs.PatientRegisterRequest toRegister(Patient patient){
        return new PatientDTOs.PatientRegisterRequest(
                patient.getFirstName(),
                patient.getMiddleName(),
                patient.getFirstSurname(),
                patient.getLastName(),
                patient.getUsername(),
                patient.getPassword(),
                patient.getUserTypeId(),
                patient.getId(),
                "PACIENTE"
        );
    }
    // --- Entidad → response ---
    public PatientResponse toResponse(Patient patient) {
        String birthDate = buildBirthDate(patient);
        return new PatientResponse(
                patient.getId(),
                patient.getFullName(),
                patient.getEmail(),
                patient.getPhone(),
                patient.getGender(),
                birthDate,
                patient.getUserTypeId(),
                patient.getState() != null ? patient.getState().name() : "ACTIVO"
        );
    }

    private String buildBirthDate(Patient patient) {
        if (patient.getBirthDay() == null || patient.getBirthMonth() == null || patient.getBirthYear() == null) {
            return null;
        }
        return patient.getBirthDay() + "/" + patient.getBirthMonth() + "/" + patient.getBirthYear();
    }
}
