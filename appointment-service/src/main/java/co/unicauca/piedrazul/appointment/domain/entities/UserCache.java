package co.unicauca.piedrazul.appointment.domain.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Copia local de los datos básicos de un usuario
 * Se actualiza cuando identity-service publica un evento de registro
 */
@Entity
@Table(name = "users_cache")
public class UserCache {

    @Id
    @Column(name = "user_id")
    private int userId;

    @Column(name = "user_full_name", length = 200)
    private String fullName;

    @Column(name = "user_role", length = 50)
    private String role;

    @Column(name = "user_state", length = 20)
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
