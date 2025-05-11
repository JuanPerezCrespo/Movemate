package es.upm.grupo19.isst.movemateback;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import es.upm.grupo19.isst.movemateback.Model.Actividad;
import es.upm.grupo19.isst.movemateback.Repository.ActividadRepository;

@DataJpaTest
public class ActividadEstadoUpdateTest {

    @Autowired
    private ActividadRepository actividadRepository;

    @Test
    public void editarActividadActualizaEstado() {
        // Crear y guardar actividad
        Actividad actividad = new Actividad();
        actividad.setDeporte("CrossFit");
        actividad.setUbicacion("Madrid Centro");
        actividad.setEstado("disponible");
        actividad.setMaxParticipantes(10);
        Actividad guardada = actividadRepository.save(actividad);

        // Cambiar estado a "completa"
        guardada.setEstado("completa");
        actividadRepository.save(guardada);

        // Recuperar y comprobar
        Optional<Actividad> actualizada = actividadRepository.findById(guardada.getId());
        assertTrue(actualizada.isPresent(), "La actividad debería existir");
        assertEquals("completa", actualizada.get().getEstado(), "El estado debería haberse actualizado correctamente");
    }
    
}
