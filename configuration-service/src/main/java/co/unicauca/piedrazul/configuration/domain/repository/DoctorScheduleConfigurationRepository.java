package co.unicauca.piedrazul.configuration.domain.repository;

import co.unicauca.piedrazul.configuration.domain.entities.DoctorScheduleConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para gestionar configuraciones de horarios de profesionales.
 *
 * @author Santiago Solarte
 */
@Repository
public interface DoctorScheduleConfigurationRepository extends JpaRepository<DoctorScheduleConfiguration, Integer> {
    
    List<DoctorScheduleConfiguration> findByDoctorId(int doctorId);
    
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM DoctorScheduleConfiguration d WHERE d.doctorId = :doctorId")
    void deleteByDoctorId(@Param("doctorId") int doctorId);
}
