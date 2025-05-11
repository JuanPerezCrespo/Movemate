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
import es.upm.grupo19.isst.movemateback.Model.Reserva;
import es.upm.grupo19.isst.movemateback.Repository.ActividadRepository;
import es.upm.grupo19.isst.movemateback.Repository.ClienteRepository;
import es.upm.grupo19.isst.movemateback.Repository.ReservaRepository;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class ReservaActividadCompletaTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private ActividadRepository actividadRepository;

    @Autowired
    private ReservaRepository reservaRepository;

    @Test
    public void noPermitirReservarActividadCompleta() throws Exception {
        // Crear cliente 1 (reserva real)
        Cliente cliente1 = new Cliente();
        cliente1.setNombre("Lucía");
        cliente1.setEmail("lucia@example.com");
        cliente1.setUsername("lucia123");
        cliente1.setPassword("claveSegura");
        clienteRepository.save(cliente1);

        // Crear cliente 2 (intentará reservar y fallar)
        Cliente cliente2 = new Cliente();
        cliente2.setNombre("Pablo");
        cliente2.setEmail("pablo@example.com");
        cliente2.setUsername("pablo123"); // o el nombre que quieras
        cliente2.setPassword("claveSegura");
        clienteRepository.save(cliente2);

        // Crear actividad con 1 plaza
        Actividad actividad = new Actividad();
        actividad.setDeporte("Padel");
        actividad.setUbicacion("Las Tablas");
        actividad.setEstado("Disponible");
        actividad.setMaxParticipantes(1);
        actividad.setFecha(LocalDateTime.now().plusDays(1));
        actividadRepository.save(actividad);

        // Hacer una reserva previa
        Reserva reserva = new Reserva();
        reserva.setCliente(cliente1);
        reserva.setActividad(actividad);
        reservaRepository.save(reserva);

        // Intentar una segunda reserva que debería fallar
        mockMvc.perform(post("/myapi/actividad/" + actividad.getId() + "/reservar/" + cliente2.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Esta actividad está llena. No es posible inscribirse en ella."));
    }
}