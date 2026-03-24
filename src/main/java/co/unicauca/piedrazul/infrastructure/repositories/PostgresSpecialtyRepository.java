/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package co.unicauca.piedrazul.infrastructure.repositories;

import co.unicauca.piedrazul.domain.acces.ISpecialtyRepository;
import co.unicauca.piedrazul.domain.entities.Specialty;
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

public class PostgresSpecialtyRepository implements ISpecialtyRepository {
    @Override
    public boolean save(Specialty specialty) {
        String sql = "INSERT INTO specialties (spec_name) VALUES (?)";
        try (Connection conn = PostgreSQLConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, specialty.getSpecialtyName());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al guardar especialidad: " + e.getMessage());
            return false;
        }
    }

    @Override
    public Specialty findById(int id) {
        String sql = "SELECT * FROM specialties WHERE spec_id = ?";
        try (Connection conn = PostgreSQLConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return mapResultSetToSpecialty(rs); 
        } catch (SQLException e) {
            System.err.println("Error al buscar especialidad: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<Specialty> findAll() {
        List<Specialty> specialties = new ArrayList<>();
        String sql = "SELECT * FROM specialties";
        try (Connection conn = PostgreSQLConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) specialties.add(mapResultSetToSpecialty(rs));
        } catch (SQLException e) {
            System.err.println("Error al listar especialidades: " + e.getMessage());
        }
        return specialties;
    }

    // Convierte una fila del ResultSet en un objeto Specialty
    private Specialty mapResultSetToSpecialty(ResultSet rs) throws SQLException {
        Specialty specialty = new Specialty();
        specialty.setSpecialtyId(rs.getInt("spec_id"));
        specialty.setSpecialtyName(rs.getString("spec_name"));
        return specialty;
    }
}
