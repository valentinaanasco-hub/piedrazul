/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package co.unicauca.piedrazul.domain.entities;

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
    public Patient() {}

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
}
