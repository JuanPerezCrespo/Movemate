package com.movemate.controller;

import java.util.List;
import com.movemate.model.Actividad;
import com.movemate.model.Usuario;
import com.movemate.model.Reserva;
import com.movemate.repository.ActividadRepository;
import com.movemate.repository.UsuarioRepository;

import com.movemate.model.Cliente;
import com.movemate.model.Monitor;

import com.movemate.repository.ReservaRepository;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
public class ActividadController {

    private final ActividadRepository actividadRepository;
    private final UsuarioRepository usuarioRepository;
    private final ReservaRepository reservaRepository;

    public ActividadController(ActividadRepository actividadRepository,
                               UsuarioRepository usuarioRepository,
                               ReservaRepository reservaRepository) {
        this.actividadRepository = actividadRepository;
        this.usuarioRepository = usuarioRepository;
        this.reservaRepository = reservaRepository;
    }

    @GetMapping("/actividades")
    public String listarActividades(Model model) {
        List<Actividad> actividades = actividadRepository.findAll();
        model.addAttribute("actividades", actividades);
        return "actividades";
    }
    // Mostrar formulario para crear una nueva actividad
    @GetMapping("/actividades/nueva")
    public String mostrarFormulario(Model model) {
        model.addAttribute("actividad", new Actividad());
        return "crear-actividad";
    }

    // Guardar actividad desde el formulario
    @PostMapping("/actividades/guardar")
    public String guardarActividad(@ModelAttribute Actividad actividad, Authentication auth) {
        String username = auth.getName();
        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername(username);
    
        if (usuarioOpt.isPresent() && usuarioOpt.get() instanceof Monitor monitor) {
            actividad.setMonitor(monitor);
        }
    
        actividadRepository.save(actividad);
        return "redirect:/actividades";
    }
    @GetMapping("/actividades/{id}")
    public String verActividad(@PathVariable Long id, Model model) {
        Optional<Actividad> actividadOpt = actividadRepository.findById(id);
    
        if (actividadOpt.isPresent()) {
            model.addAttribute("actividad", actividadOpt.get());
            return "detalle-actividad";
        } else {
            return "redirect:/actividades";
        }
    }
@PostMapping("/actividades/reservar")
public String reservarActividad(@RequestParam Long actividadId, Authentication auth) {
    Optional<Actividad> actividadOpt = actividadRepository.findById(actividadId);

    if (actividadOpt.isPresent()) {
        Actividad actividad = actividadOpt.get();
        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername(auth.getName());

        if (usuarioOpt.isPresent() && usuarioOpt.get() instanceof Cliente cliente) {
            Reserva reserva = new Reserva();
            reserva.setActividad(actividad);
            reserva.setUsuario(cliente);  // Ahora sí es tipo Cliente

            reservaRepository.save(reserva);
        } else {
            // ⚠️ No es cliente o no existe: puedes redirigir o lanzar error
            return "redirect:/actividades?error=not-client";
        }
    }

    return "redirect:/actividades/" + actividadId;
}

}
