package co.unicauca.piedrazul.appointment.application;

import co.unicauca.piedrazul.appointment.domain.entities.Appointment;
import co.unicauca.piedrazul.appointment.domain.entities.enums.AppointmentStatus;
import co.unicauca.piedrazul.appointment.domain.repository.AppointmentRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Al arrancar el servicio, republica todas las citas no canceladas a RabbitMQ
 * para que el medical-staff-service reconstruya su cache Redis de slots ocupados.
 * Esto corrige el vacío que queda tras un reinicio del contenedor.
 */
@Component
public class StartupCachePublisher implements CommandLineRunner {

    private final AppointmentRepository appointmentRepository;
    private final AppointmentEventPublisher eventPublisher;

    public StartupCachePublisher(AppointmentRepository appointmentRepository,
                                 AppointmentEventPublisher eventPublisher) {
        this.appointmentRepository = appointmentRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void run(String... args) {
        List<Appointment> active = appointmentRepository
                .findByStatusNot(AppointmentStatus.CANCELADA);

        for (Appointment appointment : active) {
            eventPublisher.publishAppointmentCreated(appointment);
        }

        System.out.printf("[StartupCachePublisher] %d citas publicadas al cache de disponibilidad%n",
                active.size());
    }
}
