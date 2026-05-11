package co.unicauca.piedrazul.medical.application;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import co.unicauca.piedrazul.events.AppointmentCreatedEvent;
import co.unicauca.piedrazul.medical.domain.entities.OccupiedSlotCache;
import co.unicauca.piedrazul.medical.domain.repository.OccupiedSlotCacheRepository;

@Component
public class AppointmentEventListener {
    
    private final OccupiedSlotCacheRepository cacheRepository;

    public AppointmentEventListener(OccupiedSlotCacheRepository cacheRepository){
        this.cacheRepository = cacheRepository;
    }
    
    @RabbitListener(queues = RabbitMQConfig.QUEUE_APPOINTMENT_CREATED)
    public void onAppointmentCreated(AppointmentCreatedEvent event){

        if("CANCELADA".equals(event.status())){
            cacheRepository.deleteByAppointmentId(event.appointmentId());
            return;
        }
        OccupiedSlotCache cache = cacheRepository.findByAppointmentId(event.appointmentId())
        .orElse(new OccupiedSlotCache());

        cache.setAppointmentId(event.appointmentId());
        cache.setDoctorId(event.doctorId());
        cache.setDate(event.date());
        cache.setStartTime(event.startTime());
        cache.setEndTime(event.endTime());
        cache.setStatus(event.status());

        cacheRepository.save(cache);
    }
}
