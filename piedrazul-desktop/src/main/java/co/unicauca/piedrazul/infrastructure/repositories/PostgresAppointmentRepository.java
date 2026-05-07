package co.unicauca.piedrazul.infrastructure.repositories;

import co.unicauca.piedrazul.domain.entities.Appointment;
import co.unicauca.piedrazul.domain.entities.Doctor;
import co.unicauca.piedrazul.domain.entities.Patient;
import co.unicauca.piedrazul.infrastructure.persistence.PostgreSQLConnection;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import co.unicauca.piedrazul.domain.access.IAppointmentRepository;
import co.unicauca.piedrazul.domain.entities.enums.AppointmentStatus;

/**
 * @author Valentina Añasco
 * @author Camila Dorado
 * @author Felipe Gutierrez
 * @author Ginner Ortega
 * @author Santiago Solarte
 */
public class PostgresAppointmentRepository implements IAppointmentRepository {

    @Override
    public boolean save(Appointment appointment) {
        String sql = "INSERT INTO appointments (appt_doct_id, appt_pat_id, appt_date, "
                + "appt_start_time, appt_end_time, appt_reason, appt_notes, appt_status) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = PostgreSQLConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, appointment.getDoctor().getId());
            pstmt.setInt(2, appointment.getPatient().getId());
            pstmt.setDate(3, Date.valueOf(appointment.getDate()));
            pstmt.setTime(4, Time.valueOf(appointment.getStartTime()));
            pstmt.setTime(5, Time.valueOf(appointment.getEndTime()));
            pstmt.setString(6, appointment.getReason());
            pstmt.setString(7, appointment.getNotes());
            pstmt.setString(8, appointment.getStatus().name());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al guardar cita: " + e.getMessage());
            return false;
        }
    }

    @Override
    public Appointment findById(int id) {
        String sql = """
        SELECT 
            a.appt_id, a.appt_date, a.appt_start_time, a.appt_end_time, a.appt_reason, a.appt_notes, a.appt_status,
            u_doc.user_id            AS doc_id,
            u_doc.user_first_name    AS doc_first_name,
            u_doc.user_middle_name   AS doc_middle_name,
            u_doc.user_first_surname AS doc_first_surname,
            u_doc.user_last_name     AS doc_last_name,
            u_pat.user_id            AS pat_id,
            u_pat.user_first_name    AS pat_first_name,
            u_pat.user_middle_name   AS pat_middle_name,
            u_pat.user_first_surname AS pat_first_surname,
            u_pat.user_last_name     AS pat_last_name,
            p.pat_phone
        FROM appointments a
        JOIN doctors  d     ON a.appt_doct_id = d.doct_user_id
        JOIN users    u_doc ON d.doct_user_id  = u_doc.user_id
        JOIN patients p     ON a.appt_pat_id   = p.pat_user_id
        JOIN users    u_pat ON p.pat_user_id   = u_pat.user_id
        WHERE a.appt_id = ?
        """;
        try (Connection conn = PostgreSQLConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToAppointment(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar cita: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<Appointment> findAll() {
        List<Appointment> appointments = new ArrayList<>();
        String sql = """
        SELECT 
            a.appt_id, a.appt_date, a.appt_start_time, a.appt_end_time, a.appt_reason, a.appt_notes, a.appt_status,
            u_doc.user_id            AS doc_id,
            u_doc.user_first_name    AS doc_first_name,
            u_doc.user_middle_name   AS doc_middle_name,
            u_doc.user_first_surname AS doc_first_surname,
            u_doc.user_last_name     AS doc_last_name,
            u_pat.user_id            AS pat_id,
            u_pat.user_first_name    AS pat_first_name,
            u_pat.user_middle_name   AS pat_middle_name,
            u_pat.user_first_surname AS pat_first_surname,
            u_pat.user_last_name     AS pat_last_name,
            p.pat_phone
        FROM appointments a
        JOIN doctors  d     ON a.appt_doct_id = d.doct_user_id
        JOIN users    u_doc ON d.doct_user_id  = u_doc.user_id
        JOIN patients p     ON a.appt_pat_id   = p.pat_user_id
        JOIN users    u_pat ON p.pat_user_id   = u_pat.user_id
        ORDER BY a.appt_date DESC, a.appt_start_time ASC
        """;
        try (Connection conn = PostgreSQLConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql); ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                appointments.add(mapResultSetToAppointment(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al listar citas: " + e.getMessage());
        }
        return appointments;
    }

    @Override
    public Appointment findByDoctorAndDateAndHour(int doctorId, String date, String startTime, String endTime) {
        String sql = """
        SELECT 
            a.appt_id, a.appt_date, a.appt_start_time, a.appt_end_time, a.appt_reason, a.appt_notes, a.appt_status,
            u_doc.user_id            AS doc_id,
            u_doc.user_first_name    AS doc_first_name,
            u_doc.user_middle_name   AS doc_middle_name,
            u_doc.user_first_surname AS doc_first_surname,
            u_doc.user_last_name     AS doc_last_name,
            u_pat.user_id            AS pat_id,
            u_pat.user_first_name    AS pat_first_name,
            u_pat.user_middle_name   AS pat_middle_name,
            u_pat.user_first_surname AS pat_first_surname,
            u_pat.user_last_name     AS pat_last_name,
            p.pat_phone
        FROM appointments a
        JOIN doctors  d     ON a.appt_doct_id = d.doct_user_id
        JOIN users    u_doc ON d.doct_user_id  = u_doc.user_id
        JOIN patients p     ON a.appt_pat_id   = p.pat_user_id
        JOIN users    u_pat ON p.pat_user_id   = u_pat.user_id
        WHERE a.appt_doct_id = ? 
          AND a.appt_date = ? 
          AND a.appt_start_time = ? 
          AND a.appt_end_time = ?
        """;
        try (Connection conn = PostgreSQLConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, doctorId);
            pstmt.setDate(2, Date.valueOf(date));
            pstmt.setTime(3, Time.valueOf(startTime));
            pstmt.setTime(4, Time.valueOf(endTime));
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToAppointment(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar cita por médico, fecha y hora: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<Appointment> findByDoctorAndDate(int doctorId, String date) {
        List<Appointment> appointments = new ArrayList<>();
        String sql = """
        SELECT 
            a.appt_id, a.appt_date, a.appt_start_time, a.appt_end_time, a.appt_reason, a.appt_notes, a.appt_status,
            u_doc.user_id            AS doc_id,
            u_doc.user_first_name    AS doc_first_name,
            u_doc.user_middle_name   AS doc_middle_name,
            u_doc.user_first_surname AS doc_first_surname,
            u_doc.user_last_name     AS doc_last_name,
            u_pat.user_id            AS pat_id,
            u_pat.user_first_name    AS pat_first_name,
            u_pat.user_middle_name   AS pat_middle_name,
            u_pat.user_first_surname AS pat_first_surname,
            u_pat.user_last_name     AS pat_last_name,
            p.pat_phone
        FROM appointments a
        JOIN doctors  d     ON a.appt_doct_id = d.doct_user_id
        JOIN users    u_doc ON d.doct_user_id  = u_doc.user_id
        JOIN patients p     ON a.appt_pat_id   = p.pat_user_id
        JOIN users    u_pat ON p.pat_user_id   = u_pat.user_id
        WHERE a.appt_doct_id = ? 
          AND a.appt_date = ?
        ORDER BY a.appt_start_time ASC
        """;
        try (Connection conn = PostgreSQLConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, doctorId);
            pstmt.setDate(2, Date.valueOf(date));
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    appointments.add(mapResultSetToAppointment(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar citas por médico y fecha: " + e.getMessage());
        }
        return appointments;
    }

    @Override
    public boolean update(Appointment appointment) {
        String sql = "UPDATE appointments SET appt_date = ?, appt_start_time = ?, "
                + "appt_end_time = ?, appt_reason = ?, appt_notes = ?, appt_status = ? WHERE appt_id = ?";
        try (Connection conn = PostgreSQLConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDate(1, Date.valueOf(appointment.getDate()));
            pstmt.setTime(2, Time.valueOf(appointment.getStartTime()));
            pstmt.setTime(3, Time.valueOf(appointment.getEndTime()));
            pstmt.setString(4, appointment.getReason());
            pstmt.setString(5, appointment.getNotes());
            pstmt.setString(6, appointment.getStatus().name());
            pstmt.setInt(7, appointment.getAppointmentId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar cita: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM appointments WHERE appt_id = ?";
        try (Connection conn = PostgreSQLConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al eliminar cita: " + e.getMessage());
            return false;
        }
    }

    private Appointment mapResultSetToAppointment(ResultSet rs) throws SQLException {
        Appointment appointment = new Appointment();
        appointment.setAppointmentId(rs.getInt("appt_id"));
        appointment.setDate(rs.getDate("appt_date").toLocalDate());
        appointment.setStartTime(rs.getTime("appt_start_time").toLocalTime());
        appointment.setEndTime(rs.getTime("appt_end_time").toLocalTime());
        appointment.setReason(rs.getString("appt_reason"));
        appointment.setNotes(rs.getString("appt_notes"));
        appointment.setStatus(AppointmentStatus.valueOf(rs.getString("appt_status")));

        Doctor doctor = new Doctor();
        doctor.setId(rs.getInt("doc_id"));
        doctor.setFirstName(rs.getString("doc_first_name"));
        doctor.setMiddleName(rs.getString("doc_middle_name"));
        doctor.setFirstSurname(rs.getString("doc_first_surname"));
        doctor.setLastName(rs.getString("doc_last_name"));
        appointment.setDoctor(doctor);

        Patient patient = new Patient();
        patient.setId(rs.getInt("pat_id"));
        patient.setFirstName(rs.getString("pat_first_name"));
        patient.setMiddleName(rs.getString("pat_middle_name"));
        patient.setFirstSurname(rs.getString("pat_first_surname"));
        patient.setLastName(rs.getString("pat_last_name"));
        patient.setPhone(rs.getString("pat_phone"));
        appointment.setPatient(patient);

        return appointment;
    }
}
