package co.unicauca.piedrazul.infrastructure.persistence;

import co.unicauca.piedrazul.domain.access.IUserRepository;
import co.unicauca.piedrazul.domain.entities.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 *
 * @author Santiago Solarte
 */
public class SqliteUserRepository implements IUserRepository {

    @Override
    public boolean save(User user) {

        String sql = """
            INSERT INTO "USER" (
                "USER_ID",
                "USER_TYPE_ID",
                "USER_FIRST_NAME",
                "USER_MIDDLE_NAME",
                "USER_FIRST_SURNAME",
                "USER_LAST_NAME",
                "USER_NAME",
                "USER_PASSWORD",
                "USER_STATE",
                "ROLE_ID",
                "EMAIL",
                "BIRTH_DATE"
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = SqliteConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, user.getId());
            pstmt.setString(2, user.getUserTypeId());
            pstmt.setString(3, user.getFirstName());
            pstmt.setString(4, user.getMiddleName());
            pstmt.setString(5, user.getFirstSurname());
            pstmt.setString(6, user.getLastName());
            pstmt.setString(7, user.getUsername());
            pstmt.setString(8, user.getPassword());
            pstmt.setString(9, user.getState());
            pstmt.setInt(10, user.getRoleId());
            pstmt.setString(11, user.getEmail());
            pstmt.setString(12, user.getBirthDate());


            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("Error al guardar usuario: " + e.getMessage());
            return false;
        }
    }


    @Override
    public User findByUsername(String username) {

        String sql = """
            SELECT * FROM "USER"
            WHERE "USER_NAME" = ?
            """;

        try (Connection conn = SqliteConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                User user = new User();

                user.setId(rs.getInt("USER_ID"));
                user.setUserTypeId(rs.getString("USER_TYPE_ID"));
                user.setFirstName(rs.getString("USER_FIRST_NAME"));
                user.setMiddleName(rs.getString("USER_MIDDLE_NAME"));
                user.setFirstSurname(rs.getString("USER_FIRST_SURNAME"));
                user.setLastName(rs.getString("USER_LAST_NAME"));
                user.setUsername(rs.getString("USER_NAME"));
                user.setPassword(rs.getString("USER_PASSWORD"));
                user.setState(rs.getString("USER_STATE"));
                user.setRoleId(rs.getInt("ROLE_ID"));
                user.setEmail(rs.getString("EMAIL"));
                user.setBirthDate(rs.getString("BIRTH_DATE"));  

                return user;
            }

        } catch (SQLException e) {
            System.err.println("Error al buscar usuario: " + e.getMessage());
        }

        return null;
    }


    @Override
    public List<User> findAll() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean update(User user) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}