package co.unicauca.piedrazul.configuration.domain.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad que representa un parámetro de configuración global del sistema.
 *
 * @author Santiago Solarte
 */
@Entity
@Table(name = "system_parameters")
public class SystemParameter {

    @Id
    @Column(name = "parameter_key", length = 100)
    private String key;

    @Column(name = "parameter_value", nullable = false, columnDefinition = "TEXT")
    private String value;

    @Column(name = "parameter_description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PreUpdate
    @PrePersist
    public void updateTimestamp() {
        this.updatedAt = LocalDateTime.now();
    }

    public SystemParameter() {}

    public SystemParameter(String key, String value, String description) {
        this.key = key;
        this.value = value;
        this.description = description;
    }

    // Getters y Setters
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
