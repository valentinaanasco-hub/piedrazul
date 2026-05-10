package co.unicauca.piedrazul.identity.domain.entities;

import jakarta.persistence.*;

/**
 * Entidad que representa un rol del sistema.
 * Mapeada a la tabla roles existente en la BD.
 *
 * @author Santiago Solarte
 */
@Entity
@Table(name = "roles")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id")
    private int roleId;

    @Column(name = "role_name", nullable = false, unique = true, length = 50)
    private String roleName;

    // --- Constructor vacío requerido por JPA ---
    public Role() {}

    public Role(String roleName) {
        this.roleName = roleName;
    }

    // --- Getters y Setters ---
    public int getRoleId() { return roleId; }
    public void setRoleId(int roleId) { this.roleId = roleId; }
    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }
}
