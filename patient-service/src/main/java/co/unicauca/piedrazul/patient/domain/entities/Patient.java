package co.unicauca.piedrazul.patient.domain.entities;

import co.unicauca.piedrazul.patient.domain.enums.UserState;
import jakarta.persistence.*;

/**
 * Entidad que representa un paciente del sistema.
 * Un paciente tiene datos en dos tablas: users (credenciales) y patients (datos clínicos).
 * Se mapea a la vista lógica unificada mediante un JOIN en el repositorio.
 *
 * @author Santiago Solarte
 */
@Entity
@Table(name = "patients")
public class Patient {

    // --- Clave primaria: es el mismo user_id ---
    @Id
    @Column(name = "pat_user_id")
    private int id;

    @Column(name = "pat_phone", nullable = false, length = 20)
    private String phone;

    @Column(name = "pat_gender", nullable = false, length = 20)
    private String gender;

    @Column(name = "pat_birth_day", length = 2)
    private String birthDay;

    @Column(name = "pat_birth_month", length = 2)
    private String birthMonth;

    @Column(name = "pat_birth_year", length = 4)
    private String birthYear;

    @Column(name = "pat_email", length = 150)
    private String email;

    // --- Datos de la tabla users (no persistidos por este servicio, solo leídos) ---
    @Transient
    private String firstName;

    @Transient
    private String middleName;

    @Transient
    private String firstSurname;

    @Transient
    private String lastName;

    @Transient
    private String username;

    @Transient
    private String password;

    @Transient
    private String userTypeId;

    @Transient
    private UserState state;

    // --- Constructor vacío requerido por JPA ---
    public Patient() {}

    // --- Getters y Setters ---
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public String getBirthDay() { return birthDay; }
    public void setBirthDay(String birthDay) { this.birthDay = birthDay; }
    public String getBirthMonth() { return birthMonth; }
    public void setBirthMonth(String birthMonth) { this.birthMonth = birthMonth; }
    public String getBirthYear() { return birthYear; }
    public void setBirthYear(String birthYear) { this.birthYear = birthYear; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getMiddleName() { return middleName; }
    public void setMiddleName(String middleName) { this.middleName = middleName; }
    public String getFirstSurname() { return firstSurname; }
    public void setFirstSurname(String firstSurname) { this.firstSurname = firstSurname; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getUserTypeId() { return userTypeId; }
    public void setUserTypeId(String userTypeId) { this.userTypeId = userTypeId; }
    public UserState getState() { return state; }
    public void setState(UserState state) { this.state = state; }

    public String getFullName() {
        StringBuilder sb = new StringBuilder();
        if (firstName != null && !firstName.isBlank()) sb.append(firstName.trim()).append(" ");
        if (middleName != null && !middleName.isBlank()) sb.append(middleName.trim()).append(" ");
        if (firstSurname != null && !firstSurname.isBlank()) sb.append(firstSurname.trim()).append(" ");
        if (lastName != null && !lastName.isBlank()) sb.append(lastName.trim()).append(" ");
        return sb.toString().trim();
    }
}
