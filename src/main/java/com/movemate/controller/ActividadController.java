package com.movemate.controller;

import com.movemate.model.*;
import com.movemate.repository.*;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
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
public String listarActividades(@RequestParam(required = false, defaultValue = "todas") String mostrar,
                                Model model) {

    List<Actividad> actividades;

    switch (mostrar) {
        case "futuras" -> 
            actividades = actividadRepository.findAll().stream()
                .filter(a -> a.getFecha().isAfter(LocalDateTime.now()))
                .filter(a -> !"Cancelada".equalsIgnoreCase(a.getEstado()))
                .toList();
        case "pasadas" -> 
            actividades = actividadRepository.findAll().stream()
                .filter(a -> a.getFecha().isBefore(LocalDateTime.now()))
                .toList();
        case "todas" -> 
            actividades = actividadRepository.findAll();
        default -> 
            actividades = actividadRepository.findAll();
    }

    model.addAttribute("actividades", actividades);
    model.addAttribute("mostrar", mostrar);
    return "actividades";
}




    @GetMapping("/actividades/nueva")
    public String mostrarFormulario(Model model) {
        model.addAttribute("actividad", new Actividad());
        return "crear-actividad";
    }

    @PostMapping("/actividades/guardar")
    public String guardarActividad(@ModelAttribute Actividad actividad,
                                   @RequestParam("imagen") MultipartFile imagen,
                                   Authentication auth) {

        String username = auth.getName();
        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername(username);

        if (usuarioOpt.isPresent() && usuarioOpt.get() instanceof Monitor monitor) {
            actividad.setMonitor(monitor);
        }

        // Guardar imagen en carpeta estática
        if (!imagen.isEmpty()) {
            String uploadsDir = "uploads/";
            File uploadsFolder = new File(uploadsDir);
            if (!uploadsFolder.exists()) {
                uploadsFolder.mkdirs();
            }

            String filename = System.currentTimeMillis() + "_" + imagen.getOriginalFilename();
            String path = uploadsDir + filename;

            try {
                Files.copy(imagen.getInputStream(), Paths.get(path), StandardCopyOption.REPLACE_EXISTING);
                actividad.setImagenUrl("/uploads/" + filename);
            } catch (IOException e) {
                e.printStackTrace();
                actividad.setImagenUrl("/images/default.jpg");
            }
        } else {
            // No se subió imagen → usar genérica
            actividad.setImagenUrl("/images/default.jpg");
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
    public String reservar(@RequestParam Long actividadId, Authentication auth, RedirectAttributes redirectAttributes) {
        String username = auth.getName();
        Usuario usuario = usuarioRepository.findByUsername(username).orElse(null);
        Actividad actividad = actividadRepository.findById(actividadId).orElse(null);

        if (usuario instanceof Cliente cliente && actividad != null) {
            boolean yaReservado = reservaRepository.existsByUsuarioAndActividad(cliente, actividad);
            if (!yaReservado) {
                Reserva reserva = new Reserva();
                reserva.setUsuario(cliente);
                reserva.setActividad(actividad);
                reservaRepository.save(reserva);

                actividad.setParticipantes(actividad.getParticipantes() + 1);
                if (actividad.getParticipantes() >= actividad.getMaxParticipantes()) {
                    actividad.setEstado("Completa");
                }

                actividadRepository.save(actividad);

                redirectAttributes.addFlashAttribute("mensaje", "✅ Te has apuntado correctamente.");
            } else {
                redirectAttributes.addFlashAttribute("mensaje", "⚠️ Ya estabas apuntado a esta actividad.");
            }
        } else {
            redirectAttributes.addFlashAttribute("mensaje", "❌ Error al apuntarse.");
        }

        return "redirect:/actividades/" + actividadId;
    }

    @PostMapping("/actividades/desapuntarse")
    public String desapuntarse(@RequestParam Long actividadId, Authentication auth, RedirectAttributes redirectAttributes) {
        String username = auth.getName();
        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername(username);
        Optional<Actividad> actividadOpt = actividadRepository.findById(actividadId);

        if (usuarioOpt.isPresent() && usuarioOpt.get() instanceof Cliente cliente && actividadOpt.isPresent()) {
            Actividad actividad = actividadOpt.get();
            List<Reserva> reservas = reservaRepository.findByUsuarioAndActividad(cliente, actividad);

            if (!reservas.isEmpty()) {
                reservas.forEach(reservaRepository::delete);

                int nuevosParticipantes = actividad.getParticipantes() - reservas.size();
                actividad.setParticipantes(Math.max(nuevosParticipantes, 0));
                actividadRepository.save(actividad);

                redirectAttributes.addFlashAttribute("mensaje", "✅ Te has desapuntado correctamente.");
            } else {
                redirectAttributes.addFlashAttribute("mensaje", "⚠️ No estabas apuntado a esta actividad.");
            }
        } else {
            redirectAttributes.addFlashAttribute("mensaje", "❌ Error al desapuntarse.");
        }

        return "redirect:/actividades/" + actividadId;
    }

    @PostMapping("/actividades/{id}/cancelar")
    public String cancelarActividad(@PathVariable Long id, Authentication auth) {
        Optional<Actividad> optActividad = actividadRepository.findById(id);
        if (optActividad.isPresent()) {
            Actividad actividad = optActividad.get();
            if (actividad.getMonitor().getUsername().equals(auth.getName())) {
                actividad.setEstado("Cancelada");
                actividadRepository.save(actividad);
            }
        }
        return "redirect:/actividades";
    }

    @GetMapping("/mis-actividades")
    public String verMisActividades(Authentication auth, Model model) {
        String username = auth.getName();
        Usuario usuario = usuarioRepository.findByUsername(username).orElse(null);

        if (usuario instanceof Cliente cliente) {
            List<Reserva> reservas = reservaRepository.findByUsuario(cliente);
            model.addAttribute("reservas", reservas);
            model.addAttribute("tipo", "cliente");
        } else if (usuario instanceof Monitor monitor) {
            List<Actividad> actividades = actividadRepository.findByMonitor(monitor);
            model.addAttribute("actividades", actividades);
            model.addAttribute("tipo", "monitor");
        }

        return "mis-actividades";
    }
    @PostMapping("/actividades/{id}/eliminar")
    public String eliminarActividad(@PathVariable Long id, Authentication auth, RedirectAttributes redirectAttributes) {
    Optional<Actividad> actividadOpt = actividadRepository.findById(id);

     if (actividadOpt.isPresent()) {
        Actividad actividad = actividadOpt.get();
        String username = auth.getName();

        // Solo el monitor que la creó puede eliminarla
        if (actividad.getMonitor().getUsername().equals(username)) {
            actividadRepository.delete(actividad);
            redirectAttributes.addFlashAttribute("mensaje", "✅ Actividad eliminada correctamente.");
        } else {
            redirectAttributes.addFlashAttribute("mensaje", "❌ No tienes permisos para eliminar esta actividad.");
        }
    }

    return "redirect:/actividades";
}
}
