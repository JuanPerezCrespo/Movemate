package com.movemate.data;

import com.movemate.model.Usuario;
import com.movemate.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        if (usuarioRepository.findAll().isEmpty()) {
            Usuario cliente = new Usuario();
            cliente.setUsername("cliente");
            cliente.setPassword(passwordEncoder.encode("cliente123"));
            cliente.setRol("ROLE_CLIENTE");

            Usuario monitor = new Usuario();
            monitor.setUsername("monitor");
            monitor.setPassword(passwordEncoder.encode("monitor123"));
            monitor.setRol("ROLE_MONITOR");

            usuarioRepository.save(cliente);
            usuarioRepository.save(monitor);

            System.out.println("✅ Usuarios iniciales insertados correctamente.");
        } else {
            System.out.println("ℹ️ Usuarios ya existen, no se insertaron nuevos.");
        }
    }
}
