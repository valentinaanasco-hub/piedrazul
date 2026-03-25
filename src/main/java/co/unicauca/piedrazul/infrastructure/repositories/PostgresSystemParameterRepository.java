package co.unicauca.piedrazul.infrastructure.repositories;

import co.unicauca.piedrazul.domain.acces.ISystemParameterRepository;
import co.unicauca.piedrazul.domain.entities.SystemParameter;
import co.unicauca.piedrazul.infrastructure.persistence.PostgreSQLConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author santi
 */
public class PostgresSystemParameterRepository implements ISystemParameterRepository {

    @Override
    public SystemParameter findByKey(String key) {
        String sql = """
                     SELECT * FROM system_parameters WHERE parameter_key = ?
                     """;
        try (Connection conn = PostgreSQLConnection.getConnection(); PreparedStatement pstm = conn.prepareStatement(sql)) {

            pstm.setString(0, key);
            ResultSet rs = pstm.executeQuery();
            if (rs.next()) {
                return mapResultSetToSystemParameter(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar parametro: " + e.getMessage());
        }
        return null;
    }
    // Convierte una fila del ResultSet en un objeto Specialty

    private SystemParameter mapResultSetToSystemParameter(ResultSet rs) throws SQLException {
        SystemParameter systemParameter = new SystemParameter();
        systemParameter.setKey(rs.getString("parameter_key"));
        systemParameter.setValue(rs.getString("parameter_value"));
        return systemParameter;
    }

}
