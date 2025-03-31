package com.movemate.controller;

import org.springframework.stereotype.Controller;
import com.movemate.model.Usuario;
import org.springframework.web.bind.annotation.GetMapping;
import com.movemate.repository.UsuarioRepository;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import jakarta.servlet.http.HttpSession;
import com.movemate.security.CustomUserDetails;
import org.springframework.security.core.context.SecurityContextHolder;


@Controller
public class UsuarioController {

    private final UsuarioRepository usuarioRepository;

    public UsuarioController(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping("/perfil")
    public String verPerfil(Authentication auth, Model model) {
        String username = auth.getName();
        Usuario usuario = usuarioRepository.findByUsername(username).orElse(null);
        model.addAttribute("usuario", usuario);
        return "perfil";
    }
    @PostMapping("/usuario/eliminar")
    public String eliminarCuenta(@AuthenticationPrincipal CustomUserDetails userDetails
    , HttpSession session) {
        Usuario usuario = userDetails.getUsuario();

        // Eliminar al usuario
        usuarioRepository.deleteById(usuario.getId());

        // Limpiar sesión y logout
        SecurityContextHolder.clearContext();
        session.invalidate();

        // Redirigir a login o página de despedida
        return "redirect:/login?cuentaEliminada";
    }
}
