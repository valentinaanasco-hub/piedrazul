package co.unicauca.piedrazul.domain.entities;

import co.unicauca.piedrazul.domain.entities.enums.UserState;
import java.util.List;

/**
 * @author Valentina Añasco
 * @author Camila Dorado
 * @author Felipe Gutierrez
 * @author Ginner Ortega
 * @author Santiago Solarte
 */
public class Patient extends User {

    private String phone;
    private String gender;
    private String birthDay;
    private String birthMonth;
    private String birthYear;
    private String email;

    // Constructor vacío (necesario para mapeo desde repositorio)
    public Patient() {
    }

    // Constructor completo (para el service)
    public Patient(String phone, String gender, String birthDay,
            String birthMonth, String birthYear, String email, int id,
            String userTypeId, String firstName, String middleName, String firstSurname,
            String lastName, String username, String password, UserState state, List<Role> roles) {
        super(id, userTypeId, firstName, middleName, firstSurname, lastName, username, password, state, roles);
        this.phone = phone;
        this.gender = gender;
        this.birthDay = birthDay;
        this.birthMonth = birthMonth;
        this.birthYear = birthYear;
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getBirthDay() {
        return birthDay;
    }

    public void setBirthDay(String birthDay) {
        this.birthDay = birthDay;
    }

    public String getBirthMonth() {
        return birthMonth;
    }

    public void setBirthMonth(String birthMonth) {
        this.birthMonth = birthMonth;
    }

    public String getBirthYear() {
        return birthYear;
    }

    public void setBirthYear(String birthYear) {
        this.birthYear = birthYear;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
