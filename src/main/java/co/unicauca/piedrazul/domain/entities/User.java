package co.unicauca.piedrazul.domain.entities;

/**
 *
 * @author Santiago Solarte
 */
public class User {
     private String primerNombre;
    private String segundoNombre;
    private String primerApellido;
    private String segundoApellido;
    private String username;
    private String password;
    private String correo;
    private String rol;
    
    public User(String primerNombre, String segundoNombre,
                String primerApellido, String segundoApellido,
                String username, String password,
                String correo, String rol) {

        this.primerNombre = primerNombre;
        this.segundoNombre = segundoNombre;
        this.primerApellido = primerApellido;
        this.segundoApellido = segundoApellido;
        this.username = username;
        this.password = password;
        this.correo = correo;
        this.rol = rol;
    }
      // getters
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getPrimerNombre() { return primerNombre; }
    public String getSegundoNombre() { return segundoNombre; }
    public String getPrimerApellido() { return primerApellido; }
    public String getSegundoApellido() { return segundoApellido; }
    public String getCorreo() { return correo; }
    public String getRol() { return rol; }
    
}
