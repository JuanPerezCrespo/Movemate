package es.upm.grupo19.isst.movemateback.Model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

@Entity
@Inheritance(strategy = jakarta.persistence.InheritanceType.JOINED)
public class Usuario {

    // Atributos de la clase Usuario:
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombre;
    private String apellidos;
    @Email // Validación de correo electrónico
    private String email;
    private String telefono;
    private String genero;
    private String direccion;
    private String latitud;
    private String longitud;
    @NotNull
    private String username;
    @NotNull
    private String password;
    private String rol; // Puede ser "usuario" o "monitor"

    // Constructor vacío:
    // Este constructor es necesario para que JPA pueda crear instancias de la clase
    // Usuario.
    public Usuario() {
    }

    // Getters y Setters:
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getGenero() {
        return genero;
    }

    public void setGenero(String genero) {
        this.genero = genero;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getLatitud() {
        return latitud;
    }

    public void setLatitud(String latitud) {
        this.latitud = latitud;
    }

    public String getLongitud() {
        return longitud;
    }

    public void setLongitud(String longitud) {
        this.longitud = longitud;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    @Override
    public String toString() {
        return "Usuario [id=" + id + ", nombre=" + nombre + ", apellidos=" + apellidos + ", email=" + email
                + ", telefono=" + telefono + ", genero=" + genero + ", direccion=" + direccion + ", latitud=" + latitud
                + ", longitud=" + longitud + ", username=" + username + ", password=" + password + ", rol=" + rol + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((nombre == null) ? 0 : nombre.hashCode());
        result = prime * result + ((apellidos == null) ? 0 : apellidos.hashCode());
        result = prime * result + ((email == null) ? 0 : email.hashCode());
        result = prime * result + ((telefono == null) ? 0 : telefono.hashCode());
        result = prime * result + ((genero == null) ? 0 : genero.hashCode());
        result = prime * result + ((direccion == null) ? 0 : direccion.hashCode());
        result = prime * result + ((latitud == null) ? 0 : latitud.hashCode());
        result = prime * result + ((longitud == null) ? 0 : longitud.hashCode());
        result = prime * result + ((username == null) ? 0 : username.hashCode());
        result = prime * result + ((password == null) ? 0 : password.hashCode());
        result = prime * result + ((rol == null) ? 0 : rol.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Usuario other = (Usuario) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (nombre == null) {
            if (other.nombre != null)
                return false;
        } else if (!nombre.equals(other.nombre))
            return false;
        if (apellidos == null) {
            if (other.apellidos != null)
                return false;
        } else if (!apellidos.equals(other.apellidos))
            return false;
        if (email == null) {
            if (other.email != null)
                return false;
        } else if (!email.equals(other.email))
            return false;
        if (telefono == null) {
            if (other.telefono != null)
                return false;
        } else if (!telefono.equals(other.telefono))
            return false;
        if (genero == null) {
            if (other.genero != null)
                return false;
        } else if (!genero.equals(other.genero))
            return false;
        if (direccion == null) {
            if (other.direccion != null)
                return false;
        } else if (!direccion.equals(other.direccion))
            return false;
        if (latitud == null) {
            if (other.latitud != null)
                return false;
        } else if (!latitud.equals(other.latitud))
            return false;
        if (longitud == null) {
            if (other.longitud != null)
                return false;
        } else if (!longitud.equals(other.longitud))
            return false;
        if (username == null) {
            if (other.username != null)
                return false;
        } else if (!username.equals(other.username))
            return false;
        if (password == null) {
            if (other.password != null)
                return false;
        } else if (!password.equals(other.password))
            return false;
        if (rol == null) {
            if (other.rol != null)
                return false;
        } else if (!rol.equals(other.rol))
            return false;
        return true;
    }

}
