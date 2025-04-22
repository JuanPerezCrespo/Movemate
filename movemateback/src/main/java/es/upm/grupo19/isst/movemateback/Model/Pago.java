package es.upm.grupo19.isst.movemateback.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.validation.constraints.PositiveOrZero;

@Entity
public class Pago {

    @OneToOne
    @JoinColumn(name = "reserva_id") // Añade una columna "reserva_id" en la tabla Pago que referencia a la tabla
                                     // Reserva.
    @JsonIgnore
    private Reserva reserva; // Relación uno a uno con la clase Reserva

    // Atributos de la clase Pago:
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String metodoPago; // Puede ser "tarjeta", "transferencia", etc.
    private String estado; // Puede ser "pendiente", "completado", "fallido", etc.

    @PositiveOrZero // Validación para asegurar que la cantidad es cero o positiva.
    private double cantidad; // Monto del pago

    // Constructor vacío:
    // Este constructor es necesario para que JPA pueda crear instancias de la clase
    // Pago.
    public Pago() {
    }

    // Getters y Setters:
    public Reserva getReserva() {
        return reserva;
    }

    public void setReserva(Reserva reserva) {
        this.reserva = reserva;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(String metodoPago) {
        this.metodoPago = metodoPago;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public double getCantidad() {
        return cantidad;
    }

    public void setCantidad(double cantidad) {
        this.cantidad = cantidad;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((reserva == null) ? 0 : reserva.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((metodoPago == null) ? 0 : metodoPago.hashCode());
        result = prime * result + ((estado == null) ? 0 : estado.hashCode());
        long temp;
        temp = Double.doubleToLongBits(cantidad);
        result = prime * result + (int) (temp ^ (temp >>> 32));
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
        Pago other = (Pago) obj;
        if (reserva == null) {
            if (other.reserva != null)
                return false;
        } else if (!reserva.equals(other.reserva))
            return false;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (metodoPago == null) {
            if (other.metodoPago != null)
                return false;
        } else if (!metodoPago.equals(other.metodoPago))
            return false;
        if (estado == null) {
            if (other.estado != null)
                return false;
        } else if (!estado.equals(other.estado))
            return false;
        if (Double.doubleToLongBits(cantidad) != Double.doubleToLongBits(other.cantidad))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "Pago [reserva=" + reserva + ", id=" + id + ", metodoPago=" + metodoPago + ", estado=" + estado
                + ", cantidad=" + cantidad + "]";
    }

}
