/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package co.unicauca.piedrazul.domain.entities;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author santi
 */
public class Doctor extends User {
    private String professionalId;
    private List<DoctorSchedule> schedules = new ArrayList<>();
    private List<Specialty> specialties = new ArrayList<>();

    public Doctor() {}

    public String getProfessionalId() { 
        return professionalId; 
    }
    public void setProfessionalId(String professionalId) { 
        this.professionalId = professionalId; 
    }
    public List<DoctorSchedule> getSchedules() { 
        return schedules; 
    }
    public void setSchedules(List<DoctorSchedule> schedules) { 
        this.schedules = schedules; 
    }
    public List<Specialty> getSpecialties() { 
        return specialties; 
    }
    public void setSpecialties(List<Specialty> specialties) { 
        this.specialties = specialties; 
    }
}
