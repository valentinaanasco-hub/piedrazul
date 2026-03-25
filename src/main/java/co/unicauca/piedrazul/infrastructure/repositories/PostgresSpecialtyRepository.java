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
        try (Connection conn = PostgreSQLConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
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
        try (Connection conn = PostgreSQLConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar especialidad: " + e.getMessage());
        }
        return null;
    }

    @Override
    public Specialty findByName(String name) {
        String sql = "SELECT * FROM specialties WHERE spec_name = ?";
        try (Connection conn = PostgreSQLConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar especialidad por nombre: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<Specialty> findAll() {
        List<Specialty> list = new ArrayList<>();
        String sql = "SELECT * FROM specialties";
        try (Connection conn = PostgreSQLConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql); ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al listar especialidades: " + e.getMessage());
        }
        return list;
    }

    @Override
    public boolean assignSpecialtyToDoctor(int doctorId, int specialtyId) {
        // ON CONFLICT DO NOTHING evita errores si ya existe la asignación
        String sql = """
            INSERT INTO doctor_specialties (ds_doct_id, ds_spec_id)
            VALUES (?, ?)
            """;
        try (Connection conn = PostgreSQLConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, doctorId);
            pstmt.setInt(2, specialtyId);
            return pstmt.executeUpdate() >= 0; // 0 si ya existía, 1 si se insertó
        } catch (SQLException e) {
            System.err.println("Error al asignar especialidad: " + e.getMessage());
            return false;
        }
    }

    @Override
    public List<Specialty> findByDoctorId(int doctorId) {
        List<Specialty> list = new ArrayList<>();
        String sql = """
            SELECT s.spec_id, s.spec_name
            FROM specialties s
            JOIN doctor_specialties ds ON s.spec_id = ds.ds_spec_id
            WHERE ds.ds_doct_id = ?
            """;
        try (Connection conn = PostgreSQLConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, doctorId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar especialidades del médico: " + e.getMessage());
        }
        return list;
    }

    private Specialty mapRow(ResultSet rs) throws SQLException {
        Specialty s = new Specialty();
        s.setSpecialtyId(rs.getInt("spec_id"));
        s.setSpecialtyName(rs.getString("spec_name"));
        return s;
    }
}
