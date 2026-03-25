package co.unicauca.piedrazul.infrastructure.repositories;

import co.unicauca.piedrazul.domain.entities.Doctor;
import co.unicauca.piedrazul.infrastructure.persistence.PostgreSQLConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import co.unicauca.piedrazul.domain.acces.IDoctorRepository;
import co.unicauca.piedrazul.domain.acces.IUserRepository;

/**
 * @author Valentina Añasco 
 * @author Camila Dorado
 * @author Felipe Gutierrez
 * @author Ginner Ortega
 * @author Santiago Solarte 
 */

public class PostgresDoctorRepository implements IDoctorRepository {
    
     @Override
    public boolean save(Doctor doctor) {
        // Todas las inserciones ocurren dentro de una sola transacción
        String sqlUser = """
            INSERT INTO users (user_id, user_username, user_password,
                user_first_name, user_middle_name, user_first_surname,
                user_last_name, user_state)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;
        String sqlDoctor = """
            INSERT INTO doctors (doct_user_id, doct_professional_id)
            VALUES (?, ?)
            """;
 
        try (Connection conn = PostgreSQLConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 1. Inserta la fila base en users
                try (PreparedStatement stmtU = conn.prepareStatement(sqlUser)) {
                    stmtU.setInt(1, doctor.getId());
                    stmtU.setString(2, doctor.getUsername());
                    stmtU.setString(3, doctor.getPassword());
                    stmtU.setString(4, doctor.getFirstName());
                    stmtU.setString(5, doctor.getMiddleName());
                    stmtU.setString(6, doctor.getFirstSurname());
                    stmtU.setString(7, doctor.getLastName());
                    stmtU.setString(8, "ACTIVO");
                    stmtU.executeUpdate();
                }
 
                // 2. Inserta la fila específica de doctors
                try (PreparedStatement stmtD = conn.prepareStatement(sqlDoctor)) {
                    stmtD.setInt(1, doctor.getId());
                    stmtD.setString(2, doctor.getProfessionalId());
                    stmtD.executeUpdate();
                }
 
                conn.commit();
                return true;
 
            } catch (SQLException e) {
                conn.rollback();
                System.err.println("Error al guardar médico (rollback): " + e.getMessage());
                return false;
            }
        } catch (SQLException e) {
            System.err.println("Error de conexión: " + e.getMessage());
            return false;
        }
    }

    @Override
    public Doctor findById(int id) {
        // JOIN para traer datos de users y doctors en una sola consulta
        String sql = "SELECT u.*, d.doct_professional_id FROM users u " +
                     "JOIN doctors d ON u.user_id = d.doct_user_id " +
                     "WHERE u.user_id = ?";
        try (Connection conn = PostgreSQLConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return mapResultSetToDoctor(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar doctor: " + e.getMessage());
        }
        return null;
    }


    @Override
    public List<Doctor> findAllActive() {
        List<Doctor> doctors = new ArrayList<>();
        // Solo médicos activos para el agendamiento de citas
        String sql = "SELECT u.*, d.doct_professional_id FROM users u " +
                     "JOIN doctors d ON u.user_id = d.doct_user_id " +
                     "WHERE u.user_state = 'ACTIVO'";
        try (Connection conn = PostgreSQLConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) doctors.add(mapResultSetToDoctor(rs));
        } catch (SQLException e) {
            System.err.println("Error al listar doctores activos: " + e.getMessage());
        }
        return doctors;
    }

    @Override
    public boolean update(Doctor doctor) {
        // Actualiza datos básicos del médico en la tabla users
        String sql = "UPDATE users SET user_first_name = ?, user_middle_name = ?, " +
                     "user_first_surname = ?, user_last_name = ?, user_state = ? " +
                     "WHERE user_id = ?";
        try (Connection conn = PostgreSQLConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, doctor.getFirstName());
            pstmt.setString(2, doctor.getMiddleName());
            pstmt.setString(3, doctor.getFirstSurname());
            pstmt.setString(4, doctor.getLastName());
            pstmt.setString(5, doctor.getState());
            pstmt.setInt(6, doctor.getId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar doctor: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean desactivate(int id) {
        String sql = "UPDATE users SET user_state = 'INACTIVO' WHERE user_id = ?";
        try (Connection conn = PostgreSQLConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al desactivar doctor: " + e.getMessage());
            return false;
        }
    }
    // Convierte una fila del ResultSet en un objeto Doctor
    private Doctor mapResultSetToDoctor(ResultSet rs) throws SQLException {
        Doctor doctor = new Doctor();
        doctor.setId(rs.getInt("user_id"));
        doctor.setUsername(rs.getString("user_username"));
        doctor.setUserTypeId(rs.getString("user_password"));
        doctor.setFirstName(rs.getString("user_first_name"));
        doctor.setMiddleName(rs.getString("user_middle_name"));
        doctor.setFirstSurname(rs.getString("user_first_surname"));
        doctor.setLastName(rs.getString("user_last_name"));
        doctor.setState(rs.getString("user_state"));
        doctor.setProfessionalId(rs.getString("doct_professional_id"));
        return doctor;
    }

    @Override
    public List<Doctor> findAll() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
}