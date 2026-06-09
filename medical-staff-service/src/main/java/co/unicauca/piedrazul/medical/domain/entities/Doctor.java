package co.unicauca.piedrazul.medical.domain.entities;

import jakarta.persistence.*;
import java.util.List;

/**
 * Entidad que representa un médico o profesional de salud.
 *
 * @author Ginner Ortega
 */
@Entity
@Table(name = "doctors")
public class Doctor {

    @Id
    @Column(name = "doct_user_id")
    private int id;

    @Column(name = "doct_professional_id", length = 50)
    private String licenseNumber;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "doctor_specialties",
            joinColumns = @JoinColumn(name = "ds_doct_id"),
            inverseJoinColumns = @JoinColumn(name = "ds_spec_id")
    )
    private List<Specialty> specialties;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "doct_type_id")
    private Type type;

    @OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DoctorSchedule> schedules;

    @Transient private String firstName;
    @Transient private String firstSurname;
    @Transient private String lastName;
    @Transient private String middleName;

    public Doctor() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getLicenseNumber() { return licenseNumber; }
    public void setLicenseNumber(String licenseNumber) { this.licenseNumber = licenseNumber; }
    public List<Specialty> getSpecialties() { return specialties; }
    public void setSpecialties(List<Specialty> specialties) { this.specialties = specialties; }
    public Type getType() { return type; }
    public void setType(Type type) { this.type = type; }
    public List<DoctorSchedule> getSchedules() { return schedules; }
    public void setSchedules(List<DoctorSchedule> schedules) { this.schedules = schedules; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getFirstSurname() { return firstSurname; }
    public void setFirstSurname(String firstSurname) { this.firstSurname = firstSurname; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getMiddleName() { return middleName; }
    public void setMiddleName(String middleName) { this.middleName = middleName; }

    public String getFullName() {
        StringBuilder sb = new StringBuilder();
        if (firstName    != null && !firstName.isBlank())    sb.append(firstName.trim()).append(" ");
        if (middleName   != null && !middleName.isBlank())   sb.append(middleName.trim()).append(" ");
        if (firstSurname != null && !firstSurname.isBlank()) sb.append(firstSurname.trim()).append(" ");
        if (lastName     != null && !lastName.isBlank())     sb.append(lastName.trim()).append(" ");
        return sb.toString().trim();
    }
}