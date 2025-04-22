package es.upm.grupo19.isst.movematefront.Model;

import java.util.List;

public class Monitor extends Usuario {

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
