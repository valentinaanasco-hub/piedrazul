package co.unicauca.piedrazul.medical.domain.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "doct_type")
public class Type {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "doct_type_id")
    private int id;

    @Column(name = "doct_type_name", nullable = false, length = 100)
    private String name;

    public Type() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
