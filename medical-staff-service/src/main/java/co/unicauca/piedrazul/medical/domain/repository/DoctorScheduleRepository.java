package co.unicauca.piedrazul.medical.domain.repository;

import co.unicauca.piedrazul.medical.domain.entities.DoctorSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio JPA para la entidad DoctorSchedule.
 *
 * @author Ginner Ortega
 */
@Repository
public interface DoctorScheduleRepository extends JpaRepository<DoctorSchedule, Integer> {

    List<DoctorSchedule> findByDoctorId(int doctorId);

    void deleteByDoctorId(int doctorId);
}
