package com.movemate.data;

import com.movemate.model.Cliente;
import com.movemate.model.Monitor;
import com.movemate.model.Actividad;
import com.movemate.model.Reserva;
import com.movemate.repository.UsuarioRepository;
import com.movemate.repository.ActividadRepository;
import com.movemate.repository.ReservaRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final ActividadRepository actividadRepository;
    private final ReservaRepository reservaRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UsuarioRepository usuarioRepository,
                           ActividadRepository actividadRepository,
                           ReservaRepository reservaRepository,
                           PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.actividadRepository = actividadRepository;
        this.reservaRepository = reservaRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        if (usuarioRepository.findAll().isEmpty()) {
            Cliente cliente = new Cliente();
            cliente.setUsername("cliente");
            cliente.setPassword(passwordEncoder.encode("cliente123"));
            cliente.setRol("ROLE_CLIENTE");

            Monitor monitor = new Monitor();
            monitor.setUsername("monitor");
            monitor.setPassword(passwordEncoder.encode("monitor123"));
            monitor.setRol("ROLE_MONITOR");

            usuarioRepository.save(cliente);
            usuarioRepository.save(monitor);

            System.out.println("✅ Usuarios iniciales insertados correctamente.");
        } else {
            System.out.println("ℹ️ Usuarios ya existen, no se insertaron nuevos.");
        }

        if (actividadRepository.findAll().isEmpty()) {
            Optional<Monitor> monitorOpt = usuarioRepository.findByUsername("monitor")
                    .filter(u -> u instanceof Monitor)
                    .map(u -> (Monitor) u);

            Optional<Cliente> clienteOpt = usuarioRepository.findByUsername("cliente")
                    .filter(u -> u instanceof Cliente)
                    .map(u -> (Cliente) u);

            if (monitorOpt.isPresent() && clienteOpt.isPresent()) {
                Monitor monitor = monitorOpt.get();
                Cliente cliente = clienteOpt.get();

                Actividad actividad = new Actividad();
                actividad.setDeporte("Yoga");
                actividad.setUbicacion("Parque Central");
                actividad.setFecha(LocalDateTime.of(2025, 4, 5, 10, 0));
                actividad.setPrecio(15.0);
                actividad.setDescripcion("Clase de yoga para todos los niveles.");
                actividad.setParticipantes(1);
                actividad.setMonitor(monitor);

                actividadRepository.save(actividad);

                Reserva reserva = new Reserva();
                reserva.setActividad(actividad);
                reserva.setUsuario(cliente);
                reservaRepository.save(reserva);

                System.out.println("✅ Actividad y reserva inicial creadas.");
            }
        } else {
            System.out.println("ℹ️ Ya existen actividades.");
        }
    }
}
