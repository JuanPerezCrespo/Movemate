package com.movemate.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
public class Cliente extends Usuario {

    private String preferencias;

    @OneToMany(mappedBy = "usuario")
    private List<Reserva> reservas;

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
}
