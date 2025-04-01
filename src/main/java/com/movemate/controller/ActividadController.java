package com.movemate.controller;

import com.movemate.model.*;
import com.movemate.repository.*;
import com.movemate.service.GeocodingService;
import com.movemate.service.ActividadService;

import org.springframework.http.ResponseEntity;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller // Indica que esta clase es un controlador de Spring, lo que permite manejar las
            // peticiones HTTP.
public class ActividadController {

    private final ActividadRepository actividadRepository; // Repositorio para acceder a las actividades
    private final UsuarioRepository usuarioRepository; // Repositorio para acceder a los usuarios
    private final ReservaRepository reservaRepository; // Repositorio para acceder a las reservas
    private final GeocodingService geocodingService;
    private final ActividadService actividadService;

    // Constructor que inyecta las dependencias necesarias para el controlador.
    public ActividadController(
            ActividadRepository actividadRepository,
            UsuarioRepository usuarioRepository,
            ReservaRepository reservaRepository,
            ActividadService actividadService,
            GeocodingService geocodingService) {
        this.actividadRepository = actividadRepository;
        this.usuarioRepository = usuarioRepository;
        this.reservaRepository = reservaRepository;
        this.geocodingService = geocodingService;
        this.actividadService = actividadService;
    }

    // Método que maneja la petición GET para listar actividades.
    // Permite filtrar las actividades por estado (futuras, pasadas o todas).
   
@GetMapping("/actividades")
public String listarActividades(
        @RequestParam(required = false) String mostrar, // Nuevo parámetro para filtrar por tiempo
        @RequestParam(required = false) Double minPrice,
        @RequestParam(required = false) Double maxPrice,
        @RequestParam(required = false) String ubicacion,
        @RequestParam(required = false) String deporte,
        Model model) {
    LocalDateTime fechaActual = LocalDateTime.now();

    // Obtener todas las actividades y aplicar filtros acumulativos
    List<Actividad> actividades = actividadRepository.findAll().stream()
            .filter(a -> {
                if ("futuras".equalsIgnoreCase(mostrar)) {
                    return a.getFecha().isAfter(fechaActual); // Filtrar actividades futuras
                } else if ("pasadas".equalsIgnoreCase(mostrar)) {
                    return a.getFecha().isBefore(fechaActual); // Filtrar actividades pasadas
                }
                return true; // Mostrar todas las actividades si no se especifica filtro
            })
            .filter(a -> minPrice == null || a.getPrecio() >= minPrice) // Filtrar por precio mínimo
            .filter(a -> maxPrice == null || a.getPrecio() <= maxPrice) // Filtrar por precio máximo
            .filter(a -> ubicacion == null || ubicacion.isEmpty() || ubicacion.equalsIgnoreCase(a.getUbicacion())) // Filtrar por ubicación
            .filter(a -> deporte == null || deporte.isEmpty() || deporte.equalsIgnoreCase(a.getDeporte())) // Filtrar por deporte
            .toList();

    // Pasar los filtros y actividades al modelo
    model.addAttribute("actividades", actividades);
    model.addAttribute("mostrar", mostrar);
    model.addAttribute("ubicacion", ubicacion);
    model.addAttribute("deporte", deporte);
    model.addAttribute("minPrice", minPrice);
    model.addAttribute("maxPrice", maxPrice);
    model.addAttribute("fechaActual", fechaActual);

    return "actividades";
}

    @GetMapping("/actividades/nueva")
    public String mostrarFormulario(Model model) {
        model.addAttribute("actividad", new Actividad());   // Crear un nuevo objeto Actividad para el formulario
        return "crear-actividad";
    }

    @PostMapping("/actividades/guardar")
    public String guardarActividad(@ModelAttribute Actividad actividad,
            @RequestParam("imagen") MultipartFile imagen,
            Authentication auth) {

        // Validar que el atributo deporte no sea nulo o vacío
        if (actividad.getDeporte() == null || actividad.getDeporte().isEmpty()) {
            throw new IllegalArgumentException("El campo 'deporte' es obligatorio.");
        }

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

        // Obtener coordenadas de la dirección ingresada
        if (actividad.getDireccion() != null && !actividad.getDireccion().isEmpty()) {
            double[] coordenadas = geocodingService.obtenerCoordenadas(actividad.getDireccion());
            actividad.setLatitud(coordenadas[0]);
            actividad.setLongitud(coordenadas[1]);
        }

        // 🔹 Guardar la actividad con coordenadas automáticas
        actividadService.guardarActividad(actividad);
        return "redirect:/actividades";
    }

    @GetMapping("/actividades/{id}")
    public String verActividad(@PathVariable Long id, Authentication auth, Model model) {
        Optional<Actividad> actividadOpt = actividadRepository.findById(id);
    
        if (actividadOpt.isPresent()) {
            Actividad actividad = actividadOpt.get();
            model.addAttribute("actividad", actividad);
    
            // Verificar si la actividad ya ha ocurrido
            boolean actividadYaOcurrida = actividad.getFecha().isBefore(LocalDateTime.now());
            model.addAttribute("actividadYaOcurrida", actividadYaOcurrida);
    
            // Verificar si el usuario está apuntado
            String username = auth.getName();
            Usuario usuario = usuarioRepository.findByUsername(username).orElse(null);
            boolean usuarioReservado = false;
    
            if (usuario instanceof Cliente cliente) {
                usuarioReservado = reservaRepository.existsByUsuarioAndActividad(cliente, actividad);
            }
    
            model.addAttribute("usuarioReservado", usuarioReservado);
            return "detalle-actividad";
        } else {
            return "redirect:/actividades"; // Redirige si la actividad no existe
        }
    }

    @PostMapping("/actividades/reservar")
public String reservar(@RequestParam Long actividadId, Authentication auth, RedirectAttributes redirectAttributes) {
    String username = auth.getName();
    Usuario usuario = usuarioRepository.findByUsername(username).orElse(null);
    Actividad actividad = actividadRepository.findById(actividadId).orElse(null);

    if (usuario instanceof Cliente cliente && actividad != null) {
        boolean yaReservado = reservaRepository.existsByUsuarioAndActividad(cliente, actividad);

        // Validar si la actividad ya ha ocurrido
        if (actividad.getFecha().isBefore(LocalDateTime.now())) {
            redirectAttributes.addFlashAttribute("mensaje", "🚫 Esta actividad ya ha ocurrido. No es posible inscribirse en ella.");
            return "redirect:/actividades/" + actividadId;
        }

        // Validar si la actividad está cancelada
        if ("Cancelada".equalsIgnoreCase(actividad.getEstado())) {
            redirectAttributes.addFlashAttribute("mensaje", "🚫 Esta actividad ha sido cancelada. No es posible inscribirse en ella.");
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

    @GetMapping("/actividades/ubicaciones")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> obtenerActividadesConUbicacion() {
        List<Actividad> actividades = actividadRepository.findAll(); // Obtener todas las actividades

        List<Map<String, Object>> respuesta = actividades.stream()
            .filter(act -> act.getLatitud() != null && act.getLongitud() != null) // Evitar actividades sin coordenadas
            .map(act -> {
                Map<String, Object> actividad = new HashMap<>();
                actividad.put("id", act.getId());
                actividad.put("deporte", act.getDeporte());
                actividad.put("latitud", act.getLatitud());
                actividad.put("longitud", act.getLongitud());
                return actividad;
            })
            .collect(Collectors.toList());

        return ResponseEntity.ok(respuesta);
    }
}
