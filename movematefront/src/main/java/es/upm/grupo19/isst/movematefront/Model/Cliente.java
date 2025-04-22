package es.upm.grupo19.isst.movematefront.Model;

import java.util.List;

public class Cliente extends Usuario {

    private List<Reserva> reservas;     // Lista de reservas asociadas al cliente (si existen)
    private String preferencias;        // Deporte favorito del cliente: futbol, baloncesto, etc.

    // Constructor vacío:
    // Este constructor es necesario para que JPA pueda crear instancias de la clase.
    public Cliente() {
        super();
    }

    public String getPreferencias() {
        return preferencias;
    }

    public void setPreferencias(String preferencias) {
        this.preferencias = preferencias;
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
        int result = super.hashCode();
        result = prime * result + ((preferencias == null) ? 0 : preferencias.hashCode());
        result = prime * result + ((reservas == null) ? 0 : reservas.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        Cliente other = (Cliente) obj;
        if (preferencias == null) {
            if (other.preferencias != null)
                return false;
        } else if (!preferencias.equals(other.preferencias))
            return false;
        if (reservas == null) {
            if (other.reservas != null)
                return false;
        } else if (!reservas.equals(other.reservas))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "Cliente [preferencias=" + preferencias + ", reservas=" + reservas + "]";
    }
}
