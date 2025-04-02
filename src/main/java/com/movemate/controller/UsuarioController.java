package com.movemate.controller;

import com.movemate.model.Actividad;
import com.movemate.model.Monitor;
import com.movemate.model.Cliente;
import com.movemate.model.Usuario;
import com.movemate.repository.ActividadRepository;
import com.movemate.repository.ReservaRepository;
import com.movemate.repository.UsuarioRepository;
import com.movemate.security.CustomUserDetails;

import jakarta.servlet.http.HttpSession;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Controller
public class UsuarioController {

    private final UsuarioRepository usuarioRepository;
    private final ReservaRepository reservaRepository;
    private final ActividadRepository actividadRepository;

    public UsuarioController(UsuarioRepository usuarioRepository,
                             ReservaRepository reservaRepository,
                             ActividadRepository actividadRepository) {
        this.usuarioRepository = usuarioRepository;
        this.reservaRepository = reservaRepository;
        this.actividadRepository = actividadRepository;
    }

    @GetMapping("/perfil")
    public String verPerfil(Authentication auth, Model model) {
        String username = auth.getName();
        Usuario usuario = usuarioRepository.findByUsername(username).orElse(null);
        model.addAttribute("usuario", usuario);
        return "perfil";
    }

    @PostMapping("/usuario/eliminar")
    @Transactional
    public String eliminarCuenta(@AuthenticationPrincipal CustomUserDetails userDetails, HttpSession session) {
        Usuario usuario = userDetails.getUsuario();

        if (usuario instanceof Cliente cliente) {
            reservaRepository.deleteAllByUsuario(cliente);
        }

        if (usuario instanceof Monitor monitor) {
            List<Actividad> actividades = actividadRepository.findByMonitor(monitor);
            actividadRepository.deleteAll(actividades);
        }

        usuarioRepository.deleteById(usuario.getId());

        SecurityContextHolder.clearContext();
        session.invalidate();

        return "redirect:/login?cuentaEliminada";
    }
}
