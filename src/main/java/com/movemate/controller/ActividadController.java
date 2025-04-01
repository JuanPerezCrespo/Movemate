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
import java.util.List;
import java.util.Optional;

@Controller // Indica que esta clase es un controlador de Spring, lo que permite manejar las
            // peticiones HTTP.
public class ActividadController {

    private final ActividadRepository actividadRepository; // Repositorio para acceder a las actividades
    private final UsuarioRepository usuarioRepository; // Repositorio para acceder a los usuarios
    private final ReservaRepository reservaRepository; // Repositorio para acceder a las reservas

    // Constructor que inyecta las dependencias necesarias para el controlador.
    public ActividadController(
            ActividadRepository actividadRepository,
            UsuarioRepository usuarioRepository,
            ReservaRepository reservaRepository) {
        this.actividadRepository = actividadRepository;
        this.usuarioRepository = usuarioRepository;
        this.reservaRepository = reservaRepository;
    }

    // Método que maneja la petición GET para listar actividades.
    // Permite filtrar las actividades por estado (futuras, pasadas o todas).
   
    @GetMapping("/actividades")
    public String listarActividades(@RequestParam(required = false, defaultValue = "todas") String mostrar, Model model) {
        LocalDateTime fechaActual = LocalDateTime.now(); // Variable con la fecha actual
        List<Actividad> actividades;
    
        switch (mostrar) {
            case "futuras" -> // Filtrar actividades con fecha posterior a la actual
                actividades = actividadRepository.findAll().stream()
                        .filter(a -> a.getFecha().isAfter(fechaActual))
                        .filter(a -> !"Cancelada".equalsIgnoreCase(a.getEstado()))
                        .toList();
            case "pasadas" -> // Filtrar actividades con fecha anterior a la actual
                actividades = actividadRepository.findAll().stream()
                        .filter(a -> a.getFecha().isBefore(fechaActual))
                        .toList();
            case "todas" -> // Mostrar todas las actividades
                actividades = actividadRepository.findAll();
            default -> // Por defecto, mostrar todas las actividades
                actividades = actividadRepository.findAll();
        }
    
        model.addAttribute("actividades", actividades);
        model.addAttribute("mostrar", mostrar);
        model.addAttribute("fechaActual", fechaActual); // Pasar la fecha actual al modelo (opcional)
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
            Actividad actividad = actividadOpt.get();
            model.addAttribute("actividad", actividad);
            model.addAttribute("monitor", actividad.getMonitor()); // Asegúrate de pasar el monitor al modelo
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
            if ("Cancelada".equalsIgnoreCase(actividad.getEstado())) {
                redirectAttributes.addFlashAttribute("mensaje", "No puedes reservar: la actividad ha sido cancelada.");
                return "redirect:/actividades/" + actividadId;
            }
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
    public String desapuntarse(@RequestParam Long actividadId, Authentication auth,
            RedirectAttributes redirectAttributes) {
        String username = auth.getName();
        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername(username);
        Optional<Actividad> actividadOpt = actividadRepository.findById(actividadId);

        if (usuarioOpt.isPresent() && usuarioOpt.get() instanceof Cliente cliente && actividadOpt.isPresent()) {
            Actividad actividad = actividadOpt.get();
            List<Reserva> reservas = reservaRepository.findByUsuarioAndActividad(cliente, actividad);

            // Verificar si el usuario tiene una reserva para esta actividad
            if (!reservas.isEmpty()) {
                // Eliminar la reserva del usuario
                reservaRepository.delete(reservas.get(0)); // Eliminar solo la primera reserva encontrada

                // Reducir el número de participantes en 1
                int nuevosParticipantes = actividad.getParticipantes() - 1;
                actividad.setParticipantes(Math.max(nuevosParticipantes, 0));

                // Cambiar el estado a "Disponible" si la actividad no está cancelada y hay plazas libres
                if (!"Cancelada".equalsIgnoreCase(actividad.getEstado())
                && nuevosParticipantes < actividad.getMaxParticipantes()) {
                    actividad.setEstado("Disponible");
                    System.out.println("🔁 Estado actualizado a: " + actividad.getEstado());
                }

                // Guardar los cambios en la base de datos
                actividadRepository.save(actividad);
                System.out.println("💾 Actividad guardada con estado: " + actividad.getEstado());

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

    @PostMapping("/actividades/{id}/habilitar")
    public String habilitarActividad(@PathVariable Long id, Authentication auth) {
        Optional<Actividad> optActividad = actividadRepository.findById(id);
        if (optActividad.isPresent()) {
            Actividad actividad = optActividad.get();
            if (actividad.getMonitor().getUsername().equals(auth.getName()) && "Cancelada".equals(actividad.getEstado())) {
                actividad.setEstado("Disponible");
                actividadRepository.save(actividad);
            }
        }
        return "redirect:/actividades";
    }

    // Método para mostrar las actividades del usuario autenticado
    // Dependiendo de si es cliente o monitor, se mostrarán reservas o actividades
    // respectivamente.
    // Se utiliza la autenticación para obtener el nombre del usuario y buscarlo en
    // la base de datos.
    // Luego, se obtienen las reservas o actividades y se añaden al modelo para ser
    // mostradas en la vista.
    // El modelo también incluye un atributo "tipo" para indicar si el usuario es
    // cliente o monitor.
    // Finalmente, se devuelve la vista "mis-actividades.html" para mostrar las
    // actividades o reservas del usuario.
    // Este método es útil para que los usuarios puedan ver sus actividades o
    // reservas pasadas y futuras.
    @GetMapping("/mis-actividades")
    public String verMisActividades(Authentication auth, Model model) {
        String username = auth.getName(); // Obtiene el nombre del usuario autenticado
        Usuario usuario = usuarioRepository.findByUsername(username).orElse(null); // Busca el usuario en la base de
                                                                                   // datos
        if (usuario instanceof Cliente cliente) { // Si el usuario es un cliente
            List<Reserva> reservas = reservaRepository.findByUsuario(cliente); // Obtiene las reservas del cliente
            model.addAttribute("reservas", reservas); // Pasa las reservas al modelo
            model.addAttribute("tipo", "cliente"); // Indica que el usuario es un cliente
            model.addAttribute("usuario", usuario); // Pasa el usuario al modelo
        } else if (usuario instanceof Monitor monitor) {
            List<Actividad> actividades = actividadRepository.findByMonitor(monitor); // Obtiene las actividades del
                                                                                      // monitor
            model.addAttribute("actividades", actividades); // Pasa las actividades al modelo
            model.addAttribute("tipo", "monitor"); // Indica que el usuario es un monitor
        }

        return "mis-actividades"; // Devuelve la vista "mis-actividades.html"
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

    @GetMapping("/actividades/{id}/editar")
    public String mostrarFormularioEdicion(@PathVariable Long id, Model model, Authentication auth) {
        Optional<Actividad> actividadOpt = actividadRepository.findById(id);

        if (actividadOpt.isPresent()) {
            Actividad actividad = actividadOpt.get();
            if (actividad.getMonitor().getUsername().equals(auth.getName())) {
                model.addAttribute("actividad", actividad);
                return "editar-actividad";
            }
        }
        return "redirect:/actividades";
    }

    @PatchMapping("/actividades/{id}/editar")
    public String editarActividad(@PathVariable Long id, @ModelAttribute Actividad actividadActualizada,
                                   @RequestParam("imagen") MultipartFile imagen, Authentication auth) {
        Optional<Actividad> actividadOpt = actividadRepository.findById(id);

        if (actividadOpt.isPresent()) {
            Actividad actividad = actividadOpt.get();
            if (actividad.getMonitor().getUsername().equals(auth.getName())) {
                actividad.setDeporte(actividadActualizada.getDeporte());
                actividad.setUbicacion(actividadActualizada.getUbicacion());
                actividad.setFecha(actividadActualizada.getFecha());
                actividad.setPrecio(actividadActualizada.getPrecio());
                actividad.setDescripcion(actividadActualizada.getDescripcion());
                actividad.setMaxParticipantes(actividadActualizada.getMaxParticipantes());

                // Actualizar imagen si se sube una nueva
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
                    }
                }

                actividadRepository.save(actividad);
            }
        }
        return "redirect:/actividades";
    }
}
