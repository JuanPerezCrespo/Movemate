package com.movemate.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
public class Monitor extends Usuario {

    @OneToMany(mappedBy = "monitor")
    private List<Actividad> actividades;

    public List<Actividad> getActividades() {
        return actividades;
    }

    public void setActividades(List<Actividad> actividades) {
        this.actividades = actividades;
    }
}