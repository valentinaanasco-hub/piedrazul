package co.unicauca.piedrazul.medical.domain.factory;

/**
 * Representa una franja horaria disponible para una cita.
 *
 * @author Ginner Ortega
 */
public class AvailabilitySlot {

    private final String time;
    private final boolean available;

    public AvailabilitySlot(String time, boolean available) {
        this.time      = time;
        this.available = available;
    }

    public String getTime() { return time; }
    public boolean isAvailable() { return available; }

    @Override
    public String toString() { return time; }
}
