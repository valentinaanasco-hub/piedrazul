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

public class SystemParameter {
    private String key;
    private String value;
  
    // Constructor vacío (necesario para mapeo desde repositorio)
    public SystemParameter() {}

    public SystemParameter(String key, String value) {
        this.key = key;
        this.value = value;
    }

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
}
