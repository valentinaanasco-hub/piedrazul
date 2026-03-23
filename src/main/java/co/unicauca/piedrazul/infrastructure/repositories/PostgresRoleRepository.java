/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package co.unicauca.piedrazul.infrastructure.repositories;

import co.unicauca.piedrazul.domain.acces.IRoleRepository;
import co.unicauca.piedrazul.domain.entities.Role;
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

public class PostgresRoleRepository implements IRoleRepository {
    
    @Override
    public boolean save(Role role) {
        String sql = "INSERT INTO roles (role_name) VALUES (?)";
        try (Connection conn = PostgreSQLConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, role.getRoleName());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al guardar rol: " + e.getMessage());
            return false;
        }
    }

    @Override
    public Role findById(int id) {
        String sql = "SELECT * FROM roles WHERE role_id = ?";
        try (Connection conn = PostgreSQLConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return mapResultSetToRole(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar rol por id: " + e.getMessage());
        }
        return null;
    }

    @Override
    public Role findByName(String name) {
        // Útil para validar si un rol existe antes de asignarlo
        String sql = "SELECT * FROM roles WHERE role_name = ?";
        try (Connection conn = PostgreSQLConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return mapResultSetToRole(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar rol por nombre: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<Role> findAll() {
        List<Role> roles = new ArrayList<>();
        String sql = "SELECT * FROM roles";
        try (Connection conn = PostgreSQLConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) roles.add(mapResultSetToRole(rs));
        } catch (SQLException e) {
            System.err.println("Error al listar roles: " + e.getMessage());
        }
        return roles;
    }

    @Override
    public boolean assignRoleToUser(int userId, int roleId) {
        // Inserta en la tabla intermedia users_roles
        String sql = "INSERT INTO users_roles (ur_user_id, ur_role_id) VALUES (?, ?)";
        try (Connection conn = PostgreSQLConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, roleId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al asignar rol al usuario: " + e.getMessage());
            return false;
        }
    }

    @Override
    public List<Role> findRolesByUserId(int userId) {
        List<Role> roles = new ArrayList<>();
        // JOIN con la tabla intermedia para obtener los roles del usuario
        String sql = "SELECT r.* FROM roles r " +
                     "JOIN users_roles ur ON r.role_id = ur.ur_role_id " +
                     "WHERE ur.ur_user_id = ?";
        try (Connection conn = PostgreSQLConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) roles.add(mapResultSetToRole(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar roles del usuario: " + e.getMessage());
        }
        return roles;
    }

    // Convierte una fila del ResultSet en un objeto Role
    private Role mapResultSetToRole(ResultSet rs) throws SQLException {
        Role role = new Role();
        role.setRoleId(rs.getInt("role_id"));
        role.setRoleName(rs.getString("role_name"));
        return role;
    }
}
