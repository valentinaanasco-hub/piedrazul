package co.unicauca.piedrazul.appointment.domain.repository;

import co.unicauca.piedrazul.appointment.domain.entities.Appointment;
import co.unicauca.piedrazul.appointment.domain.entities.enums.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para la entidad Appointment.
 *
 * @author Valentina Añasco
 * @author Camila Dorado
 * @author Felipe Gutierrez
 * @author Ginner Ortega
 * @author Santiago Solarte
 */
@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Integer> {

    // Listar citas de un médico en una fecha específica
    List<Appointment> findByDoctorIdAndDateOrderByStartTimeAsc(int doctorId, LocalDate date);

    // Citas activas de un médico en una fecha (excluye canceladas)
    List<Appointment> findByDoctorIdAndDateAndStatusNot(int doctorId, LocalDate date, AppointmentStatus status);

    // Verificar si ya existe una cita en ese horario exacto
    Optional<Appointment> findByDoctorIdAndDateAndStartTime(int doctorId, LocalDate date, LocalTime startTime);

    // Listar todas las citas de un paciente
    List<Appointment> findByPatientIdOrderByDateDescStartTimeAsc(int patientId);

    // Listar todas las citas activas
    List<Appointment> findByStatusNot(AppointmentStatus status);
}
