package es.upm.grupo19.isst.movemateback.Controller;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.slf4j.*;

import es.upm.grupo19.isst.movemateback.Config.GeocodingService;
import es.upm.grupo19.isst.movemateback.Model.Cliente;
import es.upm.grupo19.isst.movemateback.Model.Reserva;
import es.upm.grupo19.isst.movemateback.Repository.ClienteRepository;
import es.upm.grupo19.isst.movemateback.Repository.UsuarioRepository;

@CrossOrigin
@RestController
@RequestMapping("/myapi/cliente")
public class ClienteController {

    private final ClienteRepository clienteRepository;
    private final UsuarioRepository usuarioRepository;
    private final GeocodingService geocodingService;

    public static final Logger log = LoggerFactory.getLogger(ClienteController.class);

    public ClienteController(ClienteRepository clienteRepository, UsuarioRepository usuarioRepository,
            GeocodingService geocodingService) {
        this.geocodingService = geocodingService;
        this.usuarioRepository = usuarioRepository;
        this.clienteRepository = clienteRepository;
    }

    // Endpoint para obtener todos los clientes.
    @GetMapping
    public List<Cliente> getAllClientes() {
        return (List<Cliente>) clienteRepository.findAll();
    }

    // Endpoint para obtener un cliente por su ID, si no existe devuelve null.
    @GetMapping("/{id}")
    public Cliente getClienteById(@PathVariable Long id) {
        return clienteRepository.findById(id).orElse(null);
    }

    // Endpoint para crear un nuevo cliente.
    // Devuelve un URI con la ubicación del nuevo cliente creado.
    @PostMapping
    public ResponseEntity<?> createCliente(@RequestBody Cliente newcliente) throws URISyntaxException {
        if (newcliente.getTelefono() == null || newcliente.getTelefono().isBlank()) {
            return ResponseEntity.badRequest().body("El teléfono es obligatorio.");
        }
        if (newcliente.getNombre() == null || newcliente.getNombre().isBlank()) {
            return ResponseEntity.badRequest().body("El nombre es obligatorio.");
        }
        if (newcliente.getApellidos() == null || newcliente.getApellidos().isBlank()) {
            return ResponseEntity.badRequest().body("Los apellidos son obligatorios.");
        }
        // Comprobamos si el cliente ya existe en la base de datos.
        if (usuarioRepository.findByUsername(newcliente.getUsername()) != null) {
            log.error("El cliente ya existe: " + newcliente.getUsername());
            return ResponseEntity.badRequest().body("El nombre de usuario ya está en uso. Por favor, elige otro.");
        }
        // Comprobamos que no se repita el email.
        if (usuarioRepository.findByEmail(newcliente.getEmail()) != null) {
            log.error("El cliente ya existe: " + newcliente.getEmail());
            return ResponseEntity.badRequest().body("El email ya está en uso. Por favor, elige otro.");
        }
        // Comprobamos que la contraseña tenga al menos 8 caracteres.
        if (newcliente.getPassword().length() < 8) {
            log.error("La contraseña es demasiado corta: " + newcliente.getPassword());
            return ResponseEntity.badRequest().body("La contraseña debe tener al menos 5 caracteres.");
        }
        // Comprobamos que el nombre de usuario tenga al menos 5 caracteres.
        if (newcliente.getUsername().length() < 5) {
            log.error("El nombre de usuario es demasiado corto: " + newcliente.getUsername());
            return ResponseEntity.badRequest().body("El nombre de usuario debe tener al menos 5 caracteres.");
        }
        // Comprobamos que el email tenga un formato válido.
        if (!newcliente.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            log.error("El email no es válido: " + newcliente.getEmail());
            return ResponseEntity.badRequest().body("El email no es válido.");
        }
        // Comprobamos que el telefono tenga un formato válido y que no sea nulo ni
        // vacío.
        if (!newcliente.getTelefono().matches("^[0-9]{9}$")) {
            log.error("El teléfono no es válido: " + newcliente.getTelefono());
            return ResponseEntity.badRequest().body("El teléfono no es válido.");
        }
        // Comprobamos que el nombre y apellidos tengan un formato válido (con tildes).
        if (!newcliente.getNombre().matches("^[A-Za-zñÑáéíóúÁÉÍÓÚ ]+$")) {
            log.error("El nombre no es válido: " + newcliente.getNombre());
            return ResponseEntity.badRequest().body("El nombre no es válido.");
        }
        if (!newcliente.getApellidos().matches("^[A-Za-zñÑáéíóúÁÉÍÓÚ ]+$")) {
            log.error("Los apellidos no son válidos: " + newcliente.getApellidos());
            return ResponseEntity.badRequest().body("Los apellidos no son válidos.");
        }
        // Comprobamos que el nombre de usuario no tenga espacios.
        if (newcliente.getUsername().contains(" ")) {
            log.error("El nombre de usuario no puede contener espacios: " + newcliente.getUsername());
            return ResponseEntity.badRequest().body("El nombre de usuario no puede contener espacios.");
        }
        List<String> deportesValidos = List.of("fútbol", "baloncesto", "tenis", "running", "natación", "ciclismo");
        if (!deportesValidos.contains(newcliente.getPreferencias().toLowerCase())) {
            return ResponseEntity.badRequest().body("El deporte de preferencia no es válido.");
        }
        // Asignamos la latitud y longitud en función de la dirección.
        if (newcliente.getDireccion() != null && !newcliente.getDireccion().isEmpty()) {
            GeocodingService.Coordenadas coords = geocodingService
                    .obtenerCoordenadas(newcliente.getDireccion());
            if (coords != null) {
                newcliente.setLatitud(coords.getLat());
                newcliente.setLongitud(coords.getLon());
                log.info("Coordenadas asignadas: lat=" + coords.getLat() + ", lon=" + coords.getLon());
            } else {
                log.warn("No se pudieron obtener coordenadas para la dirección: " + newcliente.getDireccion());
            }
        }
        Cliente savedCliente = clienteRepository.save(newcliente);
        log.info("Cliente creado: " + savedCliente.getId());
        return ResponseEntity.created(new URI("/api/cliente/" + savedCliente.getId())).body(savedCliente);
    }

    // Endpoint para editar la dirección de un cliente por su ID.
    @PutMapping("/{id}/direccion")
    public ResponseEntity<?> updateClienteDireccion(@PathVariable Long id, @RequestBody String nuevaDireccion) {
        Cliente cliente = clienteRepository.findById(id).orElse(null);
        if (cliente == null) {
            return ResponseEntity.badRequest().body("El cliente no existe");
        }
        // Asignamos la latitud y longitud en función de la nueva dirección.
        GeocodingService.Coordenadas coords = geocodingService.obtenerCoordenadas(nuevaDireccion);
        if (coords != null) {
            cliente.setLatitud(coords.getLat());
            cliente.setLongitud(coords.getLon());
            log.info("Coordenadas asignadas: lat=" + coords.getLat() + ", lon=" + coords.getLon());
        } else {
            log.warn("No se pudieron obtener coordenadas para la dirección: " + nuevaDireccion);
        }
        cliente.setDireccion(nuevaDireccion);
        clienteRepository.save(cliente);
        return ResponseEntity.ok(cliente);
    }

    // Endpoint para obtener las reservas de un cliente por su ID.
    @GetMapping("/{id}/reservas")
    public List<Reserva> getReservasByClienteId(@PathVariable Long id) {
        Cliente cliente = clienteRepository.findById(id).orElse(null);
        if (cliente != null) {
            return cliente.getReservas();
        } else {
            return null;
        }
    }

}
