package com.movemate.controller;

import org.springframework.stereotype.Controller;
import com.movemate.model.Usuario;
import org.springframework.web.bind.annotation.GetMapping;
import com.movemate.repository.UsuarioRepository;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;

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
}
