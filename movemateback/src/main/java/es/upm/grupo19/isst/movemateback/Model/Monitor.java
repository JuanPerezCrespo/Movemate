package es.upm.grupo19.isst.movemateback.Model;

import jakarta.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import java.util.List;

@Entity
public class Monitor extends Usuario {

    @JsonIgnore
    @OneToMany(mappedBy = "monitor")
    @JsonManagedReference
    private List<Actividad> actividades; // Lista de actividades que el monitor ha creado

    public Monitor() {
        super();
    }

    public List<Actividad> getActividades() {
        return actividades;
    }

    public void setActividades(List<Actividad> actividades) {
        this.actividades = actividades;
    }

    @Override
    public String toString() {
        return "Monitor [actividades=" + actividades + "]";
    }
}
