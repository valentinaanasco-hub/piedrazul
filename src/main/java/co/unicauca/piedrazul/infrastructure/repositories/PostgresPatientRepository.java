/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package co.unicauca.piedrazul.infrastructure.repositories;

import co.unicauca.piedrazul.domain.acces.IPatientRepository;
import co.unicauca.piedrazul.domain.acces.IUserRepository;
import co.unicauca.piedrazul.domain.entities.Patient;
import co.unicauca.piedrazul.infrastructure.persistence.PostgreSQLConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Valentina Añasco 
 * @author Camila Dorado
 * @author Felipe Gutierrez
 * @author Ginner Ortega
 * @author Santiago Solarte 
 */

public class PostgresPatientRepository implements IPatientRepository{
   
      // Reutiliza UserRepository para no repetir lógica de users
    private final IUserRepository userRepository;

    // Inyección por constructor
    public PostgresPatientRepository(IUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public boolean save(Patient patient) {
        // Primero delega la inserción en users al UserRepository
        boolean userSaved = userRepository.save(patient);
        if (!userSaved) {
            System.err.println("Error al guardar el usuario base del paciente");
            return false;
        }

        // Luego inserta solo los datos exclusivos del paciente
        String sql = "INSERT INTO patients (pat_user_id, pat_phone, pat_gender, " +
                     "pat_birth_day, pat_birth_month, pat_birth_year, pat_email) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = PostgreSQLConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, patient.getId());
            pstmt.setString(2, patient.getPhone());
            pstmt.setString(3, patient.getGender());
            pstmt.setString(4, patient.getBirthDay());
            pstmt.setString(5, patient.getBirthMonth());
            pstmt.setString(6, patient.getBirthYear());
            pstmt.setString(7, patient.getEmail());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al guardar paciente: " + e.getMessage());
            return false;
        }
    }

    @Override
    public Patient findById(int id) {
        // JOIN para traer datos de users y patients en una sola consulta
        String sql = "SELECT u.*, p.pat_phone, p.pat_gender, p.pat_birth_day, " +
                     "p.pat_birth_month, p.pat_birth_year, p.pat_email " +
                     "FROM users u JOIN patients p ON u.user_id = p.pat_user_id " +
                     "WHERE u.user_id = ?";
        try (Connection conn = PostgreSQLConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return mapResultSetToPatient(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar paciente: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<Patient> findAll() {
        List<Patient> patients = new ArrayList<>();
        // JOIN para obtener todos los pacientes con sus datos de usuario
        String sql = "SELECT u.*, p.pat_phone, p.pat_gender, p.pat_birth_day, " +
                     "p.pat_birth_month, p.pat_birth_year, p.pat_email " +
                     "FROM users u JOIN patients p ON u.user_id = p.pat_user_id";
        try (Connection conn = PostgreSQLConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) patients.add(mapResultSetToPatient(rs));
        } catch (SQLException e) {
            System.err.println("Error al listar pacientes: " + e.getMessage());
        }
        return patients;
    }

    @Override
    public boolean update(Patient patient) {
        // Primero delega la actualización de datos de usuario
        boolean userUpdated = userRepository.update(patient);
        if (!userUpdated) {
            System.err.println("Error al actualizar el usuario base del paciente");
            return false;
        }

        // Luego actualiza solo los datos exclusivos del paciente
        String sql = "UPDATE patients SET pat_phone = ?, pat_gender = ?, " +
                     "pat_birth_day = ?, pat_birth_month = ?, pat_birth_year = ?, " +
                     "pat_email = ? WHERE pat_user_id = ?";
        try (Connection conn = PostgreSQLConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, patient.getPhone());
            pstmt.setString(2, patient.getGender());
            pstmt.setString(3, patient.getBirthDay());
            pstmt.setString(4, patient.getBirthMonth());
            pstmt.setString(5, patient.getBirthYear());
            pstmt.setString(6, patient.getEmail());
            pstmt.setInt(7, patient.getId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar paciente: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean desactivate(int id) {
         // Delega al UserRepository ya que el estado vive en users
        return userRepository.desactivate(id);
    }

    // Convierte una fila del ResultSet en un objeto Patient
    private Patient mapResultSetToPatient(ResultSet rs) throws SQLException {
        Patient patient = new Patient();
        patient.setId(rs.getInt("user_id"));
        patient.setUsername(rs.getString("user_username"));
        patient.setFirstName(rs.getString("user_first_name"));
        patient.setMiddleName(rs.getString("user_middle_name"));
        patient.setFirstSurname(rs.getString("user_first_surname"));
        patient.setLastName(rs.getString("user_last_name"));
        patient.setState(rs.getString("user_state"));
        patient.setPhone(rs.getString("pat_phone"));
        patient.setGender(rs.getString("pat_gender"));
        patient.setBirthDay(rs.getString("pat_birth_day"));
        patient.setBirthMonth(rs.getString("pat_birth_month"));
        patient.setBirthYear(rs.getString("pat_birth_year"));
        patient.setEmail(rs.getString("pat_email"));
        return patient;
    }
}
