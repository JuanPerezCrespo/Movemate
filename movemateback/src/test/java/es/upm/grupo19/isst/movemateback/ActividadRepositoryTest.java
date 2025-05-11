package es.upm.grupo19.isst.movemateback;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import es.upm.grupo19.isst.movemateback.Model.Actividad;
import es.upm.grupo19.isst.movemateback.Repository.ActividadRepository;

@DataJpaTest
public class ActividadRepositoryTest {

    @Autowired
    private ActividadRepository actividadRepository;

    @Test
    public void guardarYRecuperarActividad() {
        // Crear nueva actividad
        Actividad actividad = new Actividad();
        actividad.setDeporte("Yoga");
        actividad.setDescripcion("Clase de yoga para todos los niveles.");
        actividad.setUbicacion("Madrid Río");
        actividad.setEstado("disponible");
        actividad.setMaxParticipantes(20);

        // Guardar en la base de datos
        Actividad guardada = actividadRepository.save(actividad);
        Long id = guardada.getId();

        // Recuperar
        Optional<Actividad> recuperada = actividadRepository.findById(id);

        assertTrue(recuperada.isPresent(), "La actividad debería haberse guardado correctamente.");
        assertEquals("Yoga", recuperada.get().getDeporte());
        assertEquals("Madrid Río", recuperada.get().getUbicacion());
    }
}

