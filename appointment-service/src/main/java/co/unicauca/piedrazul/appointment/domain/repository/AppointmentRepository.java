package co.unicauca.piedrazul.appointment.domain.repository;

import co.unicauca.piedrazul.appointment.domain.entities.Appointment;
import co.unicauca.piedrazul.appointment.domain.entities.enums.AppointmentStatus;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para la entidad Appointment
 */
@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Integer> {

    // Listar citas de un médico en una fecha específica
    List<Appointment> findByDoctorIdAndDateOrderByStartTimeAsc(int doctorId, LocalDate date);

    // Citas activas de un médico en una fecha (excluye canceladas)
    List<Appointment> findByDoctorIdAndDateAndStatusNot(int doctorId, LocalDate date, AppointmentStatus status);

    // Bloqueo pesimista para evitar race conditions al agendar
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Appointment a WHERE a.doctorId = :doctorId AND a.date = :date AND a.status <> co.unicauca.piedrazul.appointment.domain.entities.enums.AppointmentStatus.CANCELADA")
    List<Appointment> findByDoctorIdAndDateForUpdate(@Param("doctorId") int doctorId, @Param("date") LocalDate date);

    // Verificar si ya existe una cita en ese horario exacto
    Optional<Appointment> findByDoctorIdAndDateAndStartTime(int doctorId, LocalDate date, LocalTime startTime);

    // Listar todas las citas de un paciente
    List<Appointment> findByPatientIdOrderByDateDescStartTimeAsc(int patientId);

    // Listar todas las citas activas
    List<Appointment> findByStatusNot(AppointmentStatus status);

    // Buscar citas de un paciente con un estado específico
    List<Appointment> findByPatientIdAndStatus(int patientId, AppointmentStatus status);
}
