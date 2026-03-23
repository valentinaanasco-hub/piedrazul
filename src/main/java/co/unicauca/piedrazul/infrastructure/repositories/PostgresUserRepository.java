/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package co.unicauca.piedrazul.infrastructure.repositories;

import co.unicauca.piedrazul.domain.acces.IUserRepository;
import co.unicauca.piedrazul.domain.entities.User;
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

public class PostgresUserRepository implements IUserRepository {
    
@Override
    public boolean save(User user) {
        String sql = "INSERT INTO users (user_id, user_username, user_password, user_first_name, " +
                     "user_middle_name, user_first_surname, user_last_name, user_state) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = PostgreSQLConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, user.getId());
            pstmt.setString(2, user.getUsername());
            pstmt.setString(3, user.getPassword());
            pstmt.setString(4, user.getFirstName());
            pstmt.setString(5, user.getMiddleName());
            pstmt.setString(6, user.getFirstSurname());
            pstmt.setString(7, user.getLastName());
            pstmt.setString(8, "ACTIVO");

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al insertar: " + e.getMessage());
            return false;
        }
    }

    @Override
    public User findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE user_username = ?";
        
        try (Connection conn = PostgreSQLConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setId(rs.getInt("user_id"));
                    user.setUsername(rs.getString("user_username"));
                    user.setPassword(rs.getString("user_password"));
                    user.setFirstName(rs.getString("user_first_name"));
                    user.setMiddleName(rs.getString("user_middle_name"));
                    user.setFirstSurname(rs.getString("user_first_surname"));
                    user.setLastName(rs.getString("user_last_name"));
                    user.setState(rs.getString("user_state"));
                    return user;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar usuario: " + e.getMessage());
        }
        return null;
    }
    
    @Override
    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users";

        try (Connection conn = PostgreSQLConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) { // Todo en el try con ;

            while (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("user_id"));
                user.setUsername(rs.getString("user_username"));
                user.setPassword(rs.getString("user_password"));
                user.setFirstName(rs.getString("user_first_name"));
                user.setMiddleName(rs.getString("user_middle_name"));
                user.setFirstSurname(rs.getString("user_first_surname"));
                user.setLastName(rs.getString("user_last_name"));
                user.setState(rs.getString("user_state"));
                users.add(user);
            }
        } catch (SQLException e) {
            System.err.println("Error al listar todos los usuarios: " + e.getMessage());
        }
        return users;
    }
    
    @Override
    public boolean update(User user) {
        
        String sql = "UPDATE users SET user_password = ?, user_first_name = ?, " +
                     "user_middle_name = ?, user_first_surname = ?, user_last_name = ?, user_state = ? " +
                     "WHERE user_id = ?";

        try (Connection conn = PostgreSQLConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user.getPassword());
            pstmt.setString(2, user.getFirstName());
            pstmt.setString(3, user.getMiddleName());
            pstmt.setString(4, user.getFirstSurname());
            pstmt.setString(5, user.getLastName());
            pstmt.setString(6, user.getState());
            pstmt.setInt(7, user.getId()); 

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar: " + e.getMessage());
            return false;
        }
    }
    
    
    @Override
    public boolean desactivate(int id) {
        // Cambia el estado a INACTIVO en lugar de eliminar, conservando el historial
        String sql = "UPDATE users SET user_state = 'INACTIVO' WHERE user_id = ?";
        try (Connection conn = PostgreSQLConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            // executeUpdate retorna el número de filas afectadas
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al desactivar usuario: " + e.getMessage());
            return false;
        }
    }
}
