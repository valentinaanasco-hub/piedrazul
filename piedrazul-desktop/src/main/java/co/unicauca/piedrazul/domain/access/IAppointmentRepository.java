package co.unicauca.piedrazul.domain.access;

import co.unicauca.piedrazul.domain.entities.Appointment;
import java.util.List;

/**
 * @author Valentina Añasco
 * @author Camila Dorado
 * @author Felipe Gutierrez
 * @author Ginner Ortega
 * @author Santiago Solarte
 */
public interface IAppointmentRepository {

    // Para reagendar o cambiar estado de una cita
    boolean update(Appointment appointment);

    // Para cancelar o eliminar una cita
    boolean delete(int id);

    // Para agendar una nueva cita
    boolean save(Appointment appointment);

    // Para buscar una cita por su id
    Appointment findById(int id);

    // Para listar todas las citas del sistema
    List<Appointment> findAll();

    // Para listar citas de un médico en una fecha específica (evitar duplicados)
    List<Appointment> findByDoctorAndDate(int doctorId, String date);

    Appointment findByDoctorAndDateAndHour(int doctorId, String date, String startTime, String endTime);
;
}
