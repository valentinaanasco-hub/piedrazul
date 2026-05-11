package co.unicauca.piedrazul.medical.domain.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import co.unicauca.piedrazul.medical.domain.entities.OccupiedSlotCache;

public interface OccupiedSlotCacheRepository extends CrudRepository<OccupiedSlotCache, String> {
    
    List<OccupiedSlotCache> findByDoctorIdAndDate(int doctorId, LocalDate date);
    Optional<OccupiedSlotCache> findByAppointmentId(int appointmentId);
    void deleteByAppointmentId(int appointmentId);
}
