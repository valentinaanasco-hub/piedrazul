package co.unicauca.piedrazul.domain.entities;

import co.unicauca.piedrazul.domain.entities.enums.UserState;
import java.util.List;

public class User {

    private int id;
    private String userTypeId;      // CC, TI, CE, PA
    private String firstName;
    private String middleName;
    private String firstSurname;
    private String lastName;
    private String username;
    private String password;
    private UserState state;           // ACTIVO, INACTIVO
    private List<Role> roles;

    // Constructor vacío (necesario para mapeo desde repositorio)
    public User() {
    }

    // Constructor completo (útil para creación desde Service)
    public User(int id, String userTypeId, String firstName, String middleName,
            String firstSurname, String lastName,
            String username, String password,
            UserState state, List<Role> roles) {

        this.id = id;
        this.userTypeId = userTypeId;
        this.firstName = firstName;
        this.middleName = middleName;
        this.firstSurname = firstSurname;
        this.lastName = lastName;
        this.username = username;
        this.password = password;
        this.state = state;
        this.roles = roles;
    }

    // ===== Getters y Setters =====
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUserTypeId() {
        return userTypeId;
    }

    public void setUserTypeId(String userTypeId) {
        this.userTypeId = userTypeId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getFirstSurname() {
        return firstSurname;
    }

    public void setFirstSurname(String firstSurname) {
        this.firstSurname = firstSurname;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public List<Role> getRoles() {
        return roles;
    }

    public void setRoles(List<Role> roles) {
        this.roles = roles;
    }

    // El password ya debe venir cifrado desde el Service
    public void setPassword(String password) {
        this.password = password;
    }

    public UserState getState() {
        return state;
    }

    public void setState(UserState state) {
        this.state = state;
    }

    public String getFullName() {
        // Se usa un StringBuilder para construir el nombre dinámicamente
        StringBuilder sb = new StringBuilder();

        if (firstName != null && !firstName.trim().isEmpty()) {
            sb.append(firstName.trim()).append(" ");
        }
        if (middleName != null && !middleName.trim().isEmpty()) {
            sb.append(middleName.trim()).append(" ");
        }
        if (firstSurname != null && !firstSurname.trim().isEmpty()) {
            sb.append(firstSurname.trim()).append(" ");
        }
        if (lastName != null && !lastName.trim().isEmpty()) {
            sb.append(lastName.trim()).append(" ");
        }

        // .trim() al final elimina el último espacio sobrante
        return sb.toString().trim();
    }

    /**
     * Añade un rol a la lista de roles del usuario de forma segura.
     *
     * @param role El objeto Role a añadir.
     */
    public void addRole(Role role) {
        if (this.roles == null) {
            this.roles = new java.util.ArrayList<>();
        }

        if (!this.roles.contains(role)) {
            this.roles.add(role);
        }
    }
}
