package es.upm.grupo19.isst.movematefront.Model;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.PositiveOrZero;

public class Actividad {

    private Long id;
    private String deporte; // Futbol, baloncesto, etc.
    private String ubicacion; // Madrid, Barcelona, etc.
    private String direccion; // Calle, número, etc.
    private String latitud;
    private String longitud;
    private String nivel; // Puede ser "principiante", "intermedio", "avanzado", etc.
    @PositiveOrZero // Validación para asegurar que el precio es cero o positivo.
    private double precio;
    private String descripcion;
    private String imagenUrl;
    @PositiveOrZero // Validación para asegurar que el número de participantes es cero o positivo.
    private int maxParticipantes;
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime fecha;
    private String estado; // Puede ser "disponible", "completada", "cancelada", etc.
    private Monitor monitor;
    private List<Reserva> reservas; // Reserva asociada a la actividad (si existe)

    // Constructor vacío:
    public Actividad() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDeporte() {
        return deporte;
    }

    public void setDeporte(String deporte) {
        this.deporte = deporte;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
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

    public String getNivel() {
        return nivel;
    }

    public void setNivel(String nivel) {
        this.nivel = nivel;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getImagenUrl() {
        return imagenUrl;
    }

    public void setImagenUrl(String imagenUrl) {
        this.imagenUrl = imagenUrl;
    }

    public int getMaxParticipantes() {
        return maxParticipantes;
    }

    public void setMaxParticipantes(int maxParticipantes) {
        this.maxParticipantes = maxParticipantes;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Monitor getMonitor() {
        return monitor;
    }

    public void setMonitor(Monitor monitor) {
        this.monitor = monitor;
    }

    public List<Reserva> getReservas() {
        return reservas;
    }

    public void setReservas(List<Reserva> reservas) {
        this.reservas = reservas;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((deporte == null) ? 0 : deporte.hashCode());
        result = prime * result + ((ubicacion == null) ? 0 : ubicacion.hashCode());
        result = prime * result + ((direccion == null) ? 0 : direccion.hashCode());
        result = prime * result + ((latitud == null) ? 0 : latitud.hashCode());
        result = prime * result + ((longitud == null) ? 0 : longitud.hashCode());
        result = prime * result + ((nivel == null) ? 0 : nivel.hashCode());
        long temp;
        temp = Double.doubleToLongBits(precio);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        result = prime * result + ((descripcion == null) ? 0 : descripcion.hashCode());
        result = prime * result + ((imagenUrl == null) ? 0 : imagenUrl.hashCode());
        result = prime * result + maxParticipantes;
        result = prime * result + ((fecha == null) ? 0 : fecha.hashCode());
        result = prime * result + ((estado == null) ? 0 : estado.hashCode());
        result = prime * result + ((monitor == null) ? 0 : monitor.hashCode());
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
        Actividad other = (Actividad) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (deporte == null) {
            if (other.deporte != null)
                return false;
        } else if (!deporte.equals(other.deporte))
            return false;
        if (ubicacion == null) {
            if (other.ubicacion != null)
                return false;
        } else if (!ubicacion.equals(other.ubicacion))
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
        if (nivel == null) {
            if (other.nivel != null)
                return false;
        } else if (!nivel.equals(other.nivel))
            return false;
        if (Double.doubleToLongBits(precio) != Double.doubleToLongBits(other.precio))
            return false;
        if (descripcion == null) {
            if (other.descripcion != null)
                return false;
        } else if (!descripcion.equals(other.descripcion))
            return false;
        if (imagenUrl == null) {
            if (other.imagenUrl != null)
                return false;
        } else if (!imagenUrl.equals(other.imagenUrl))
            return false;
        if (maxParticipantes != other.maxParticipantes)
            return false;
        if (fecha == null) {
            if (other.fecha != null)
                return false;
        } else if (!fecha.equals(other.fecha))
            return false;
        if (estado == null) {
            if (other.estado != null)
                return false;
        } else if (!estado.equals(other.estado))
            return false;
        if (monitor == null) {
            if (other.monitor != null)
                return false;
        } else if (!monitor.equals(other.monitor))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "Actividad [id=" + id + ", deporte=" + deporte + ", ubicacion=" + ubicacion + ", direccion=" + direccion
                + ", latitud=" + latitud + ", longitud=" + longitud + ", nivel=" + nivel + ", precio=" + precio
                + ", descripcion=" + descripcion + ", imagenUrl=" + imagenUrl + ", maxParticipantes=" + maxParticipantes
                + ", fecha=" + fecha + ", estado=" + estado + "]";
    }

}
