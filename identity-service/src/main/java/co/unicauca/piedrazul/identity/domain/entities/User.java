package co.unicauca.piedrazul.identity.domain.entities;

import co.unicauca.piedrazul.identity.domain.enums.UserState;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad que representa un usuario del sistema.
 * Mapeada a la tabla users existente en la BD.
 *
 * @author Santiago Solarte
 */
@Entity
@Table(name = "users")
public class User {

    @Id
    @Column(name = "user_id")
    private long id;

    @Column(name = "user_username", nullable = false, unique = true, length = 150)
    private String username;

    @Column(name = "user_password", nullable = false, length = 255)
    private String password;

    @Column(name = "user_first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "user_middle_name", length = 100)
    private String middleName;

    @Column(name = "user_first_surname", nullable = false, length = 100)
    private String firstSurname;

    @Column(name = "user_last_name", length = 100)
    private String lastName;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_state", length = 20)
    private UserState state = UserState.ACTIVO;

    @Column(name = "user_type_id", nullable = false, length = 5)
    private String userTypeId;

    // --- Relación Many-to-Many con roles via tabla users_roles ---
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "users_roles",
            joinColumns = @JoinColumn(name = "ur_user_id"),
            inverseJoinColumns = @JoinColumn(name = "ur_role_id")
    )
    private List<Role> roles = new ArrayList<>();

    // --- Constructor vacío requerido por JPA ---
    public User() {}

    // --- Getters y Setters ---
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getMiddleName() { return middleName; }
    public void setMiddleName(String middleName) { this.middleName = middleName; }
    public String getFirstSurname() { return firstSurname; }
    public void setFirstSurname(String firstSurname) { this.firstSurname = firstSurname; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public UserState getState() { return state; }
    public void setState(UserState state) { this.state = state; }
    public String getUserTypeId() { return userTypeId; }
    public void setUserTypeId(String userTypeId) { this.userTypeId = userTypeId; }
    public List<Role> getRoles() { return roles; }
    public void setRoles(List<Role> roles) { this.roles = roles; }

    public String getFullName() {
        StringBuilder sb = new StringBuilder();
        if (firstName != null && !firstName.isBlank()) sb.append(firstName.trim()).append(" ");
        if (middleName != null && !middleName.isBlank()) sb.append(middleName.trim()).append(" ");
        if (firstSurname != null && !firstSurname.isBlank()) sb.append(firstSurname.trim()).append(" ");
        if (lastName != null && !lastName.isBlank()) sb.append(lastName.trim()).append(" ");
        return sb.toString().trim();
    }
}
