package es.upm.grupo19.isst.movemateback;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import es.upm.grupo19.isst.movemateback.Model.Actividad;
import es.upm.grupo19.isst.movemateback.Model.Cliente;
import es.upm.grupo19.isst.movemateback.Model.Reserva;
import es.upm.grupo19.isst.movemateback.Repository.ActividadRepository;
import es.upm.grupo19.isst.movemateback.Repository.ClienteRepository;
import es.upm.grupo19.isst.movemateback.Repository.ReservaRepository;

@DataJpaTest
public class ReservaRepositoryTest {

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private ActividadRepository actividadRepository;

    @Autowired
    private ReservaRepository reservaRepository;

    @Test
    public void verificarReservaPorClienteYActividad() {
        // Crear cliente
        Cliente cliente = new Cliente();
        cliente.setNombre("Marta");
        cliente.setEmail("marta@example.com");
        cliente.setUsername("lucia123");
        cliente.setPassword("claveSegura");
        clienteRepository.save(cliente);

        // Crear actividad
        Actividad actividad = new Actividad();
        actividad.setDeporte("Pilates");
        actividad.setUbicacion("Retiro");
        actividad.setEstado("disponible");
        actividadRepository.save(actividad);

        // Crear reserva
        Reserva reserva = new Reserva();
        reserva.setCliente(cliente);
        reserva.setActividad(actividad);
        reservaRepository.save(reserva);

        // Buscar por cliente y actividad
        Reserva resultado = reservaRepository.findByClienteAndActividad(cliente, actividad);

        assertNotNull(resultado, "La reserva debería encontrarse por cliente y actividad");
        assertEquals(cliente.getEmail(), resultado.getCliente().getEmail());
        assertEquals(actividad.getDeporte(), resultado.getActividad().getDeporte());
    }
}
