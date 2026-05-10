package co.unicauca.piedrazul.medical.domain.entities;

import jakarta.persistence.*;
import java.util.List;

/**
 * Entidad que representa una especialidad médica.
 *
 * @author Ginner Ortega
 */
@Entity
@Table(name = "specialties")
public class Specialty {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "spec_id")
    private int id;

    @Column(name = "spec_name", nullable = false, length = 100)
    private String name;

    @ManyToMany(mappedBy = "specialties")
    private List<Doctor> doctors;

    public Specialty() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public List<Doctor> getDoctors() { return doctors; }
    public void setDoctors(List<Doctor> doctors) { this.doctors = doctors; }
}