package co.unicauca.piedrazul.appointment.domain.model;

/**
 * Representación de dominio de un usuario en caché.
 * Sin dependencias de Redis ni de frameworks.
 */
public class UserCache {

    private long userId;
    private String fullName;
    private String role;
    private String state;

    public UserCache() {}

    public boolean isActive() {
        return "ACTIVO".equalsIgnoreCase(state);
    }

    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }
    public String getFullName() { return fullName; }
    public void setFullName(String name)  { this.fullName = name; }
    public String getRole() { return role; }
    public void setRole(String role)      { this.role = role; }
    public String getState()              { return state; }
    public void setState(String state)    { this.state = state; }
}
