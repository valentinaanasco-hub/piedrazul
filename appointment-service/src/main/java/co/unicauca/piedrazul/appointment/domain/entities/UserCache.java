package co.unicauca.piedrazul.appointment.domain.entities;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;

/**
 * Copia local de los datos básicos de un usuario almacenada en Redis
 * Se actualiza cuando identity-service publica un evento de registro
 */
@RedisHash("users_cache")
public class UserCache implements Serializable {

    @Id
    private int userId;

    private String fullName;
    private String role;
    private String state;

    public UserCache() {}

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public boolean isActive() {
        return "ACTIVO".equals(state);
    }
}
