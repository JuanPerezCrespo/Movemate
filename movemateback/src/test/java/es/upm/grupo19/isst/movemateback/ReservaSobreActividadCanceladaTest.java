package es.upm.grupo19.isst.movemateback;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import es.upm.grupo19.isst.movemateback.Model.Actividad;
import es.upm.grupo19.isst.movemateback.Model.Cliente;
import es.upm.grupo19.isst.movemateback.Repository.ActividadRepository;
import es.upm.grupo19.isst.movemateback.Repository.ClienteRepository;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class ReservaSobreActividadCanceladaTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private ActividadRepository actividadRepository;

    @Test
    public void noPermitirReservarActividadCancelada() throws Exception {
        // Crear cliente
        Cliente cliente = new Cliente();
        cliente.setNombre("Sergio");
        cliente.setEmail("sergio@example.com");
        cliente.setUsername("sergio123"); 
        cliente.setPassword("claveSegura");
        clienteRepository.save(cliente);

        // Crear actividad con estado "Cancelada"
        Actividad actividad = new Actividad();
        actividad.setDeporte("Boxeo");
        actividad.setUbicacion("Vallecas");
        actividad.setEstado("Cancelada");
        actividad.setFecha(LocalDateTime.now().plusDays(1)); // actividad futura
        actividadRepository.save(actividad);

        // Intentar reservar vía MockMvc (simula llamada REST)
        mockMvc.perform(post("/myapi/actividad/" + actividad.getId() + "/reservar/" + cliente.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Esta actividad ha sido cancelada. No es posible inscribirse en ella."));
    }
}