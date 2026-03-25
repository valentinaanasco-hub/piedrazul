package co.unicauca.piedrazul.infrastructure.repositories;

import co.unicauca.piedrazul.domain.acces.IDoctorScheduleRepository;
import co.unicauca.piedrazul.domain.entities.DoctorSchedule;
import co.unicauca.piedrazul.infrastructure.persistence.PostgreSQLConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author santi
 */
public class PostgresDoctorScheduleRepository implements IDoctorScheduleRepository {

    @Override
    public boolean save(DoctorSchedule schedule, int doctorId) {
        String sql = "INSERT INTO doctor_schedules (sched_doctor_id, sched_day_of_week, "
                + "sched_start_time, sched_end_time, sched_interval_minutes) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = PostgreSQLConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, doctorId);
            pstmt.setInt(2, schedule.getDayOfWeek());
            // LocalTime se guarda como texto en BD (formato HH:mm)
            pstmt.setString(3, schedule.getStartTime().toString());
            pstmt.setString(4, schedule.getEndTime().toString());
            pstmt.setInt(5, schedule.getIntervalMinutes());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al guardar horario: " + e.getMessage());
            return false;
        }
    }

    @Override
    public List<DoctorSchedule> findByDoctorId(int doctorId) {
        List<DoctorSchedule> schedules = new ArrayList<>();
        String sql = "SELECT * FROM doctor_schedules WHERE sched_doctor_id = ?";
        try (Connection conn = PostgreSQLConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, doctorId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    schedules.add(mapResultSetToSchedule(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar horarios: " + e.getMessage());
        }
        return schedules;
    }

    @Override
    public boolean update(DoctorSchedule schedule) {
        String sql = "UPDATE doctor_schedules SET sched_day_of_week = ?, "
                + "sched_start_time = ?, sched_end_time = ?, "
                + "sched_interval_minutes = ? WHERE sched_id = ?";
        try (Connection conn = PostgreSQLConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, schedule.getDayOfWeek());
            pstmt.setString(2, schedule.getStartTime().toString());
            pstmt.setString(3, schedule.getEndTime().toString());
            pstmt.setInt(4, schedule.getIntervalMinutes());
            pstmt.setInt(5, schedule.getScheduleId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar horario: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean delete(int scheduleId) {
        String sql = "DELETE FROM doctor_schedules WHERE sched_id = ?";
        try (Connection conn = PostgreSQLConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, scheduleId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al eliminar horario: " + e.getMessage());
            return false;
        }
    }

    // Convierte una fila del ResultSet en un objeto DoctorSchedule
    // LocalTime.parse() convierte el texto "HH:mm" de vuelta a LocalTime
    private DoctorSchedule mapResultSetToSchedule(ResultSet rs) throws SQLException {
        DoctorSchedule schedule = new DoctorSchedule();
        schedule.setScheduleId(rs.getInt("sched_id"));
        schedule.setDayOfWeek(rs.getInt("sched_day_of_week"));
        schedule.setStartTime(LocalTime.parse(rs.getString("sched_start_time")));
        schedule.setEndTime(LocalTime.parse(rs.getString("sched_end_time")));
        schedule.setIntervalMinutes(rs.getInt("sched_interval_minutes"));
        return schedule;
    }
}
