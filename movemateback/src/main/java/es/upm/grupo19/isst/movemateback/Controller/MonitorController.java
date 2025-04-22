package es.upm.grupo19.isst.movemateback.Controller;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import es.upm.grupo19.isst.movemateback.Config.GeocodingService;
import es.upm.grupo19.isst.movemateback.Model.Actividad;
import es.upm.grupo19.isst.movemateback.Model.Monitor;
import es.upm.grupo19.isst.movemateback.Repository.ActividadRepository;
import es.upm.grupo19.isst.movemateback.Repository.MonitorRepository;
import es.upm.grupo19.isst.movemateback.Repository.UsuarioRepository;

@CrossOrigin
@RestController
@RequestMapping("/myapi/monitor")
public class MonitorController {

    private final MonitorRepository monitorRepository;
    private final UsuarioRepository usuarioRepository;
    private final ActividadRepository actividadRepository;
    private final GeocodingService geocodingService;
    public static final Logger log = LoggerFactory.getLogger(MonitorController.class);

    public MonitorController(MonitorRepository monitorRepository, UsuarioRepository usuarioRepository,
            ActividadRepository actividadRepository, GeocodingService geocodingService) {
        this.geocodingService = geocodingService;
        this.usuarioRepository = usuarioRepository;
        this.actividadRepository = actividadRepository;
        this.monitorRepository = monitorRepository;
    }

    // Endpoint para obtener todos los monitores.
    @GetMapping
    public List<Monitor> getAllMonitores() {
        return (List<Monitor>) monitorRepository.findAll();
    }

    // Endpoint para obtener un monitor por su ID, si no existe devuelve null.
    @GetMapping("/{id}")
    public Monitor getMonitorById(@PathVariable Long id) {
        return monitorRepository.findById(id).orElse(null);
    }

    // Endpoint para crear un nuevo monitor.
    // Devuelve un URI con la ubicación del nuevo monitor creado.
    @PostMapping
    public ResponseEntity<?> createMonitor(@RequestBody Monitor newMonitor) throws URISyntaxException {
        // Comprobamos si el monitor ya existe en la base de datos.
        if (usuarioRepository.findByUsername(newMonitor.getUsername()) != null) {
            log.error("El monitor ya existe: " + newMonitor.getUsername());
            return ResponseEntity.badRequest().body("El nombre de usuario ya está en uso. Por favor, elige otro.");
        }
        // Comprobamos que no se repita el email.
        if (usuarioRepository.findByEmail(newMonitor.getEmail()) != null) {
            log.error("El monitor ya existe: " + newMonitor.getEmail());
            return ResponseEntity.badRequest().body("El email ya está en uso. Por favor, elige otro.");
        }
        // Comprobamos que la contraseña tenga al menos 8 caracteres.
        if (newMonitor.getPassword().length() < 8) {
            log.error("La contraseña es demasiado corta: " + newMonitor.getPassword());
            return ResponseEntity.badRequest().body("La contraseña debe tener al menos 8 caracteres.");
        }
        // Comprobamos que el nombre de usuario tenga al menos 5 caracteres.
        if (newMonitor.getUsername().length() < 5) {
            log.error("El nombre de usuario es demasiado corto: " + newMonitor.getUsername());
            return ResponseEntity.badRequest().body("El nombre de usuario debe tener al menos 5 caracteres.");
        }
        // Asignamos la latitud y longitud en función de la dirección.
        if (newMonitor.getDireccion() != null && !newMonitor.getDireccion().isEmpty()) {
            GeocodingService.Coordenadas coords = geocodingService
                    .obtenerCoordenadas(newMonitor.getDireccion());
            if (coords != null) {
                newMonitor.setLatitud(coords.getLat());
                newMonitor.setLongitud(coords.getLon());
                log.info("Coordenadas asignadas: lat=" + coords.getLat() + ", lon=" + coords.getLon());
            } else {
                log.warn("No se pudieron obtener coordenadas para la dirección: " + newMonitor.getDireccion());
            }
        }
        Monitor savedMonitor = monitorRepository.save(newMonitor);
        log.info("Monitor creado: " + savedMonitor.getId());
        return ResponseEntity.created(new URI("/api/monitor/" + savedMonitor.getId())).body(savedMonitor);
    }

    // Endpoint para editat la dirección de un monitor por su ID.
    @PutMapping("/{id}/direccion")
    public ResponseEntity<?> updateMonitorDireccion(@PathVariable Long id, @RequestBody String nuevaDireccion) {
        Monitor monitor = monitorRepository.findById(id).orElse(null);
        if (monitor == null) {
            log.error("Monitor no encontrado con ID: " + id);
            return ResponseEntity.badRequest().body("Monitor no encontrado");
        }
        monitor.setDireccion(nuevaDireccion);
        // Obtener coordenadas de la nueva dirección
        GeocodingService.Coordenadas coords = geocodingService.obtenerCoordenadas(nuevaDireccion);
        if (coords != null) {
            monitor.setLatitud(coords.getLat());
            monitor.setLongitud(coords.getLon());
            log.info("Coordenadas actualizadas: lat=" + coords.getLat() + ", lon=" + coords.getLon());
        } else {
            log.warn("No se pudieron obtener coordenadas para la dirección: " + nuevaDireccion);
        }
        monitorRepository.save(monitor);
        log.info("Dirección del monitor actualizada: " + id);
        return ResponseEntity.ok(monitor);
    }

    @PostMapping("/{monitorId}/actividades")
    public ResponseEntity<?> crearActividad(@PathVariable Long monitorId, @RequestBody Actividad nuevaActividad) {
        // Buscar el monitor por su ID
        Monitor monitor = monitorRepository.findById(monitorId).orElse(null);
        if (monitor == null) {
            log.error("Monitor no encontrado con ID: " + monitorId);
            return ResponseEntity.badRequest().body("Monitor no encontrado");
        }

        // Asignar el monitor y estado inicial
        nuevaActividad.setMonitor(monitor);
        nuevaActividad.setEstado("Disponible");

        // Obtener coordenadas de la dirección si está presente
        if (nuevaActividad.getDireccion() != null && !nuevaActividad.getDireccion().isEmpty()) {
            GeocodingService.Coordenadas coords = geocodingService.obtenerCoordenadas(nuevaActividad.getDireccion());
            if (coords != null) {
                nuevaActividad.setLatitud(coords.getLat());
                nuevaActividad.setLongitud(coords.getLon());
                log.info("Coordenadas asignadas: lat=" + coords.getLat() + ", lon=" + coords.getLon());
            } else {
                log.warn("No se pudieron obtener coordenadas para la dirección: " + nuevaActividad.getDireccion());
            }
        }

        // Guardar la actividad
        Actividad actividadGuardada = actividadRepository.save(nuevaActividad);
        log.info("Actividad creada con ID: " + actividadGuardada.getId() + " para el monitor: " + monitorId);

        return ResponseEntity.ok(actividadGuardada);
    }

    // Endpoint para obtener todas las actividades de un monitor por su ID.
    @GetMapping("/{monitorId}/actividades")
    public List<Actividad> getActividadesByMonitorId(@PathVariable Long monitorId) {
        Monitor monitor = monitorRepository.findById(monitorId).orElse(null);
        if (monitor == null) {
            log.error("Monitor no encontrado con ID: " + monitorId);
            return null;
        }
        return actividadRepository.findByMonitorId(monitorId);
    }
}
