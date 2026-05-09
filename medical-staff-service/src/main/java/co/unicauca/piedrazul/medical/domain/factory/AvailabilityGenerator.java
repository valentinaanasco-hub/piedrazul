package co.unicauca.piedrazul.medical.domain.factory;

import co.unicauca.piedrazul.medical.domain.entities.DoctorSchedule;
import java.util.List;

/**
 * Interfaz del patrón Factory Method para generación de disponibilidad.
 * Cada implementación define cómo calcular las franjas horarias
 * disponibles según el tipo de horario del médico.
 *
 * @author Ginner Ortega
 */
public interface AvailabilityGenerator {

    /**
     * Genera las franjas horarias disponibles para una fecha dada.
     *
     * @param schedule  Horario del médico para ese día.
     * @param occupiedSlots Franjas ya ocupadas por citas existentes.
     * @return Lista de franjas disponibles.
     */
    List<AvailabilitySlot> generate(DoctorSchedule schedule, List<String> occupiedSlots);
}
