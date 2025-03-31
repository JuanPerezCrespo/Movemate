package com.movemate.controller;

import com.movemate.model.Cliente;
import com.movemate.model.Monitor;
import com.movemate.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class RegistroController {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public RegistroController(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/registro")
    public String mostrarFormularioRegistro(Model model) {
        model.addAttribute("cliente", new Cliente());
        return "registro";
    }

    @PostMapping("/registro")
public String procesarRegistro(
    @RequestParam String username,
    @RequestParam String password,
    @RequestParam String nombre,
    @RequestParam String email,
    @RequestParam String telefono,
    @RequestParam String genero,
    @RequestParam String direccion,
    @RequestParam String esMonitor,
    @RequestParam(required = false) String deporteMonitor) {

        if (esMonitor.equals("true")) {
        Monitor monitor = new Monitor();
        monitor.setUsername(username);
        monitor.setPassword(passwordEncoder.encode(password));
        monitor.setRol("ROLE_MONITOR");
        monitor.setNombre(nombre);
        monitor.setEmail(email);
        monitor.setTelefono(telefono);
        monitor.setGenero(genero);
        monitor.setDireccion(direccion);
        monitor.setDeporte(deporteMonitor);
        usuarioRepository.save(monitor);
    } else {
        Cliente cliente = new Cliente();
        cliente.setUsername(username);
        cliente.setPassword(passwordEncoder.encode(password));
        cliente.setRol("ROLE_CLIENTE");
        cliente.setNombre(nombre);
        cliente.setEmail(email);
        cliente.setTelefono(telefono);
        cliente.setGenero(genero);
        cliente.setDireccion(direccion);
        usuarioRepository.save(cliente);
    }

    return "redirect:/login";
}
}
