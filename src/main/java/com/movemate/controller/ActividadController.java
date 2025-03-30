package com.movemate.controller;

import java.util.List;
import com.movemate.model.Actividad;
import com.movemate.model.Usuario;
import com.movemate.model.Reserva;
import com.movemate.repository.ActividadRepository;
import com.movemate.repository.UsuarioRepository;
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
    public String guardarActividad(@ModelAttribute Actividad actividad) {
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
        Usuario usuario = usuarioRepository.findByUsername(auth.getName()).get();


        Reserva reserva = new Reserva();
        reserva.setActividad(actividad);
        reserva.setUsuario(usuario);

        reservaRepository.save(reserva);
    }

    return "redirect:/actividades/" + actividadId;
}

}
