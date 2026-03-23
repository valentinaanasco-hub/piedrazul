/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package co.unicauca.piedrazul.infrastructure.repositories;

import co.unicauca.piedrazul.domain.acces.IAppointmentRepository;
import co.unicauca.piedrazul.domain.entities.Appointment;
import co.unicauca.piedrazul.domain.entities.Doctor;
import co.unicauca.piedrazul.domain.entities.Patient;
import co.unicauca.piedrazul.infrastructure.persistence.PostgreSQLConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

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
        String sql = "INSERT INTO appointments (appt_doct_id, appt_pat_id, appt_date, " +
                     "appt_start_time, appt_end_time, appt_status) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = PostgreSQLConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, appointment.getDoctor().getId());
            pstmt.setInt(2, appointment.getPatient().getId());
            // LocalDate y LocalTime se guardan como texto en la BD
            pstmt.setString(3, appointment.getDate().toString());
            pstmt.setString(4, appointment.getStartTime().toString());
            pstmt.setString(5, appointment.getEndTime().toString());
            pstmt.setString(6, appointment.getStatus());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al guardar cita: " + e.getMessage());
            return false;
        }
    }

    @Override
    public Appointment findById(int id) {
        String sql = "SELECT * FROM appointments WHERE appt_id = ?";
        try (Connection conn = PostgreSQLConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return mapResultSetToAppointment(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar cita: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<Appointment> findAll() {
        List<Appointment> appointments = new ArrayList<>();
        String sql = "SELECT * FROM appointments";
        try (Connection conn = PostgreSQLConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) appointments.add(mapResultSetToAppointment(rs));
        } catch (SQLException e) {
            System.err.println("Error al listar citas: " + e.getMessage());
        }
        return appointments;
    }

    @Override
    public Appointment findByDoctorAndDateAndHour(int doctorId, String date, String startTime, String endTime) {
        // Busca si ya existe una cita con exactamente ese médico, fecha y horario
        String sql = "SELECT * FROM appointments " +
                     "WHERE appt_doct_id = ? " +
                     "AND appt_date = ? " +
                     "AND appt_start_time = ? " +
                     "AND appt_end_time = ?";
        try (Connection conn = PostgreSQLConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, doctorId);
            pstmt.setString(2, date);
            pstmt.setString(3, startTime);
            pstmt.setString(4, endTime);
            try (ResultSet rs = pstmt.executeQuery()) {
                // Retorna la cita si existe, null si el horario está libre
                if (rs.next()) return mapResultSetToAppointment(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar cita por médico y fecha: " + e.getMessage());
        }
        return null;
    }
    
     
        @Override
    public List<Appointment> findByDoctorAndDate(int doctorId, String date) {
        // Busca si ya existe una cita con exactamente ese médico, fecha y horario
        String sql = "SELECT * FROM appointments " +
                     "WHERE appt_doct_id = ? " +
                     "AND appt_date = ? ";
        try (Connection conn = PostgreSQLConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, doctorId);
            pstmt.setString(2, date);
            List<Appointment> appointments = new ArrayList<>();
            try (ResultSet rs = pstmt.executeQuery()) {
                // Retorna la cita si existe, null si el horario está libre
                while(rs.next()){
                    appointments.add(mapResultSetToAppointment(rs));
                }
            return appointments;
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar cita por médico y fecha: " + e.getMessage());
        }
        return null;
    }

    @Override
    public boolean update(Appointment appointment) {
        // Usado para reagendar o cambiar el estado de una cita
        String sql = "UPDATE appointments SET appt_date = ?, appt_start_time = ?, " +
                     "appt_end_time = ?, appt_status = ? WHERE appt_id = ?";
        try (Connection conn = PostgreSQLConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, appointment.getDate().toString());
            pstmt.setString(2, appointment.getStartTime().toString());
            pstmt.setString(3, appointment.getEndTime().toString());
            pstmt.setString(4, appointment.getStatus());
            pstmt.setInt(5, appointment.getAppointmentId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar cita: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM appointments WHERE appt_id = ?";
        try (Connection conn = PostgreSQLConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al eliminar cita: " + e.getMessage());
            return false;
        }
    }

    // Convierte una fila del ResultSet en un objeto Appointment
    // Los textos de fecha y hora se parsean de vuelta a LocalDate y LocalTime
    private Appointment mapResultSetToAppointment(ResultSet rs) throws SQLException {
        Appointment appointment = new Appointment();
        appointment.setAppointmentId(rs.getInt("appt_id"));
        appointment.setDate(LocalDate.parse(rs.getString("appt_date")));
        appointment.setStartTime(LocalTime.parse(rs.getString("appt_start_time")));
        appointment.setEndTime(LocalTime.parse(rs.getString("appt_end_time")));
        appointment.setStatus(rs.getString("appt_status"));

        // Solo asigna los ids para no hacer JOIN complejo aquí
        Doctor doctor = new Doctor();
        doctor.setId(rs.getInt("appt_doct_id"));
        appointment.setDoctor(doctor);

        Patient patient = new Patient();
        patient.setId(rs.getInt("appt_pat_id"));
        appointment.setPatient(patient);

        return appointment;
    }
}
