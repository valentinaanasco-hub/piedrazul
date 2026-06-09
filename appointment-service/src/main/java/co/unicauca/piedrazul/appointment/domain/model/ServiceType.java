package co.unicauca.piedrazul.appointment.domain.model;

/**
 * Servicios que ofrece el centro médico Piedrazul.
 * Determina qué tipo de atención se prestará en la cita.
 *
 * Reglas de asignación por tipo de profesional:
 *  - Sin especialidad (Médico o Terapeuta): CONSULTA_GENERAL, FISIOTERAPIA
 *  - Con especialidad Quiropraxia          : QUIROPRAXIA
 *  - Con especialidad Terapia Neural       : TERAPIA_NEURAL
 */
public enum ServiceType {
    CONSULTA_GENERAL,
    FISIOTERAPIA,
    QUIROPRAXIA,
    TERAPIA_NEURAL
}
