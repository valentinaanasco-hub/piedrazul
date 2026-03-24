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
public class Role {
    private int roleId;
    private String roleName;
    
    // Constructor vacío (necesario para mapeo desde repositorio)
    public Role() {}

    public Role(int roleId, String roleName) {
        this.roleId = roleId;
        this.roleName = roleName;
    }

    public int getRoleId() { 
        return roleId; 
    }
    public void setRoleId(int roleId) { 
        this.roleId = roleId; 
    }
    public String getRoleName() { 
        return roleName; 
    }
    public void setRoleName(String roleName) { 
        this.roleName = roleName; 
    }
}
