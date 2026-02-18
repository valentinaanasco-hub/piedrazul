package co.unicauca.piedrazul.domain.entities;

/**
 * Entidad del dominio User No depende de infraestructura. Representa el
 * concepto de Usuario dentro del sistema.
 *
 * @author Santiago Solarte
 */
public class User {

    private int id;
    private String userTypeId;      // CC, TI, CE, PA
    private String firstName;
    private String middleName;
    private String firstSurname;
    private String lastName;
    private String username;
    private String password;
    private String state;           // ACTIVE, INACTIVE
    private int roleId;
    private String email;
    private String birthDate;

    // Constructor vacío (necesario para mapeo desde repositorio)
    public User() {
    }

    // Constructor completo (útil para creación desde Service)
    public User(int id, String userTypeId, String firstName, String middleName,
            String firstSurname, String lastName,
            String username, String password,
            String state, int roleId, String email, String birthDate) {

        this.id = id;
        this.userTypeId = userTypeId;
        this.firstName = firstName;
        this.middleName = middleName;
        this.firstSurname = firstSurname;
        this.lastName = lastName;
        this.username = username;
        this.password = password;
        this.state = state;
        this.roleId = roleId;
        this.email = email;
        this.birthDate = birthDate;
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

    // El password ya debe venir cifrado desde el Service
    public void setPassword(String password) {
        this.password = password;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public int getRoleId() {
        return roleId;
    }

    public void setRoleId(int roleId) {
        this.roleId = roleId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    // Método útil en dominio
    public String getFullName() {
    // Usamos un StringBuilder para construir el nombre dinámicamente
    StringBuilder sb = new StringBuilder();
    
    if (firstName != null && !firstName.trim().isEmpty()) sb.append(firstName.trim()).append(" ");
    if (middleName != null && !middleName.trim().isEmpty()) sb.append(middleName.trim()).append(" ");
    if (firstSurname != null && !firstSurname.trim().isEmpty()) sb.append(firstSurname.trim()).append(" ");
    if (lastName != null && !lastName.trim().isEmpty()) sb.append(lastName.trim()).append(" ");
    
    // .trim() al final elimina el último espacio sobrante
    return sb.toString().trim();
}
}
