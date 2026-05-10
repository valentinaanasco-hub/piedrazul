package co.unicauca.piedrazul.medical.domain.factory;

import co.unicauca.piedrazul.medical.domain.entities.DoctorSchedule;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementación estándar del Factory Method para generación de disponibilidad.
 * Genera franjas horarias en intervalos regulares entre la hora de inicio
 * y la hora de fin del médico, excluyendo las ya ocupadas.
 *
 * @author Ginner Ortega
 */
@Component
public class StandardAvailabilityGenerator implements AvailabilityGenerator {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    @Override
    public List<AvailabilitySlot> generate(DoctorSchedule schedule, List<String> occupiedSlots) {
        List<AvailabilitySlot> slots = new ArrayList<>();

        LocalTime current  = schedule.getStartTime();
        LocalTime end      = schedule.getEndTime();
        int       interval = schedule.getIntervalMinutes();

        while (current.isBefore(end)) {
            String  timeStr  = current.format(TIME_FORMAT);
            boolean occupied = occupiedSlots != null && occupiedSlots.contains(timeStr);
            slots.add(new AvailabilitySlot(timeStr, !occupied));
            current = current.plusMinutes(interval);
        }

        return slots;
    }
}