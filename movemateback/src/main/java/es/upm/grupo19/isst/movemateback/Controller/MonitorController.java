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
        // Validaciones similares a las del cliente
        if (newMonitor.getTelefono() == null || newMonitor.getTelefono().isBlank()) {
            return ResponseEntity.badRequest().body("El teléfono es obligatorio.");
        }
        if (newMonitor.getNombre() == null || newMonitor.getNombre().isBlank()) {
            return ResponseEntity.badRequest().body("El nombre es obligatorio.");
        }
        if (newMonitor.getApellidos() == null || newMonitor.getApellidos().isBlank()) {
            return ResponseEntity.badRequest().body("Los apellidos son obligatorios.");
        }
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
        // Comprobamos que el email tenga un formato válido.
        if (!newMonitor.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            log.error("El email no es válido: " + newMonitor.getEmail());
            return ResponseEntity.badRequest().body("El email no es válido.");
        }
        // Comprobamos que el teléfono tenga un formato válido.
        if (!newMonitor.getTelefono().matches("^[0-9]{9}$")) {
            log.error("El teléfono no es válido: " + newMonitor.getTelefono());
            return ResponseEntity.badRequest().body("El teléfono no es válido.");
        }
        // Comprobamos que el nombre y apellidos tengan un formato válido (con tildes).
        if (!newMonitor.getNombre().matches("^[A-Za-zñÑáéíóúÁÉÍÓÚ ]+$")) {
            log.error("El nombre no es válido: " + newMonitor.getNombre());
            return ResponseEntity.badRequest().body("El nombre no es válido.");
        }
        if (!newMonitor.getApellidos().matches("^[A-Za-zñÑáéíóúÁÉÍÓÚ ]+$")) {
            log.error("Los apellidos no son válidos: " + newMonitor.getApellidos());
            return ResponseEntity.badRequest().body("Los apellidos no son válidos.");
        }
        // Comprobamos que el nombre de usuario no tenga espacios.
        if (newMonitor.getUsername().contains(" ")) {
            log.error("El nombre de usuario no puede contener espacios: " + newMonitor.getUsername());
            return ResponseEntity.badRequest().body("El nombre de usuario no puede contener espacios.");
        }

        // Asignamos la latitud y longitud en función de la dirección.
        if (newMonitor.getDireccion() != null && !newMonitor.getDireccion().isEmpty()) {
            GeocodingService.Coordenadas coords = geocodingService.obtenerCoordenadas(newMonitor.getDireccion());
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
        if (nuevaActividad.getFecha() == null) {
            return ResponseEntity.badRequest().body("La fecha de la actividad no puede ser nula.");
        }
        // Validar que el deporte este en la lista de deportes permitidos
        List<String> deportesPermitidos = List.of("Fútbol", "Baloncesto", "Tenis", "Running", "Natación", "Ciclismo");
        if (!deportesPermitidos.contains(nuevaActividad.getDeporte())) {
            log.error("Deporte no permitido: " + nuevaActividad.getDeporte());
            return ResponseEntity.badRequest().body("Deporte no permitido");
        }
        // Si la fecha de la actividad es anterior a la fecha actual, devolver error
        if (nuevaActividad.getFecha().isBefore(java.time.LocalDate.now().atStartOfDay())) {
            log.error("La fecha de la actividad no puede ser anterior a la fecha actual.");
            return ResponseEntity.badRequest()
                    .body("La fecha de la actividad no puede ser anterior a la fecha actual.");
        }

        // Validar que el precio sea mayor que 0
        if (nuevaActividad.getPrecio() <= 0) {
            log.error("El precio de la actividad debe ser mayor que 0.");
            return ResponseEntity.badRequest().body("El precio de la actividad debe ser mayor que 0.");
        }

        // Validar que el número máximo de participantes sea mayor que 0
        if (nuevaActividad.getMaxParticipantes() <= 0) {
            log.error("El número máximo de participantes debe ser mayor que 0.");
            return ResponseEntity.badRequest().body("El número máximo de participantes debe ser mayor que 0.");
        }

        // Validar que la dirección no esté vacía
        if (nuevaActividad.getDireccion() == null || nuevaActividad.getDireccion().isEmpty()) {
            log.error("La dirección es obligatoria.");
            return ResponseEntity.badRequest().body("La dirección es obligatoria.");
        }

        // Validar que la descripción no esté vacía
        if (nuevaActividad.getDescripcion() == null || nuevaActividad.getDescripcion().isEmpty()) {
            log.error("La descripción es obligatoria.");
            return ResponseEntity.badRequest().body("La descripción es obligatoria.");
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
