package es.upm.grupo19.isst.movematefront.Model;

public class Reserva {

    // Atributos de la reserva:
    private Long id;
    private Cliente cliente;
    private Actividad actividad;
    private Pago pago;

    public Reserva() {
        super();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public Actividad getActividad() {
        return actividad;
    }

    public void setActividad(Actividad actividad) {
        this.actividad = actividad;
    }

    public Pago getPago() {
        return pago;
    }

    public void setPago(Pago pago) {
        this.pago = pago;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((cliente == null) ? 0 : cliente.hashCode());
        result = prime * result + ((actividad == null) ? 0 : actividad.hashCode());
        result = prime * result + ((pago == null) ? 0 : pago.hashCode());
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
        Reserva other = (Reserva) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (cliente == null) {
            if (other.cliente != null)
                return false;
        } else if (!cliente.equals(other.cliente))
            return false;
        if (actividad == null) {
            if (other.actividad != null)
                return false;
        } else if (!actividad.equals(other.actividad))
            return false;
        if (pago == null) {
            if (other.pago != null)
                return false;
        } else if (!pago.equals(other.pago))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "Reserva [id=" + id + ", cliente=" + cliente + ", actividad=" + actividad + ", pago=" + pago + "]";
    }
}
