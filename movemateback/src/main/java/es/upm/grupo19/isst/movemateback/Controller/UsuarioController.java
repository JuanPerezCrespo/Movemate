package es.upm.grupo19.isst.movemateback.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.slf4j.*;

import es.upm.grupo19.isst.movemateback.Model.Cliente;
import es.upm.grupo19.isst.movemateback.Model.Monitor;
import es.upm.grupo19.isst.movemateback.Model.Usuario;
import es.upm.grupo19.isst.movemateback.Repository.ActividadRepository;
import es.upm.grupo19.isst.movemateback.Repository.ReservaRepository;
import es.upm.grupo19.isst.movemateback.Repository.UsuarioRepository;

@CrossOrigin
@RestController
@RequestMapping("/myapi/usuario")
public class UsuarioController {

    private final UsuarioRepository usuarioRepository;
    private final ReservaRepository reservaRepository;
    private final ActividadRepository actividadRepository;
    public static final Logger log = LoggerFactory.getLogger(UsuarioController.class);

    public UsuarioController(UsuarioRepository usuarioRepository, ReservaRepository reservaRepository,
            ActividadRepository actividadRepository) {
        this.actividadRepository = actividadRepository;
        this.reservaRepository = reservaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    // Endpoint para obtener todos los usuarios.
    @GetMapping
    public Iterable<Usuario> getAllUsuarios() {
        return usuarioRepository.findAll();
    }

    // Endpoint para obtener el usuario por su nombre de usuario.
    // Si no existe devuelve null.
    @GetMapping("/{username}")
    public Usuario getUsuarioByUsername(@PathVariable String username) {
        return usuarioRepository.findByUsername(username);
    }

    // Enpoint para crear un nuevo usuario.
    // Si el usuario ya existe, devuelve un mensaje de error.
    // Si el usuario no existe, lo crea y devuelve el usuario creado.
    @PostMapping("/crear")
    public ResponseEntity<?> crearUsuario(@RequestBody Usuario nuevoUsuario) {
        Usuario usuarioExistente = usuarioRepository.findByUsername(nuevoUsuario.getUsername());
        log.info("Me pasan desde el front el nuevo usuario: " + nuevoUsuario.getUsername());
        if (usuarioExistente != null) {
            return ResponseEntity.badRequest().body("El usuario ya existe");
        }

        usuarioRepository.save(nuevoUsuario);
        return ResponseEntity.ok(nuevoUsuario);
    }

    @PostMapping()
    public ResponseEntity<?> validarCredenciales(@RequestParam String username, @RequestParam String password) {
        Usuario usuarioExistente = usuarioRepository.findByUsername(username);
        log.info("Me pasan desde el front el username: " + username);
        log.info("Me pasan desde el front la password: " + password);
        if (usuarioExistente == null) {
            return ResponseEntity.badRequest().body("Usuario incorrecto");
        }

        if (!usuarioExistente.getPassword().equals(password)) {
            return ResponseEntity.badRequest().body("Contraseña incorrecta");
        }

        return ResponseEntity.ok("Autenticación exitosa");
    }

    // Endpoint para eliminar un usuario por su id.
    @DeleteMapping("/{id}/eliminar")
    @Transactional
    public ResponseEntity<?> eliminarUsuario(@PathVariable Long id) {
        Usuario usuario = usuarioRepository.findById(id).orElse(null);
        if (usuario == null) {
            return ResponseEntity.badRequest().body("El usuario no existe");
        }

        // Eliminar todas las reservas asociadas al cliente
        if (usuario instanceof Cliente cliente) {
            reservaRepository.deleteAll(cliente.getReservas());
        }

        // Si somos un monitor, eliminar todas las actividades asociadas al monitor y
        // sus reservas
        if (usuario instanceof Monitor monitor) {
            // Eliminar todas las reservas asociadas a las actividades del monitor
            reservaRepository.deleteByActividadMonitorId(monitor.getId());
            // Eliminar todas las actividades asociadas al monitor
            actividadRepository.deleteByMonitorId(monitor.getId());
        }

        // Eliminar el usuario
        usuarioRepository.delete(usuario);
        return ResponseEntity.ok("Usuario eliminado correctamente");
    }

    // Request param extrae los valores de la URL y los pasa como parámetros al
    // método.
    // Por ejemplo, si la URL es /myapi/usuario/validar?username=pepe&password=1234,
    // username tendrá el valor "pepe" y password tendrá el valor "1234".

    // Request body extrae el cuerpo de la petición y lo pasa como parámetro al
    // método.
    // Por ejemplo, si la petición es un POST con el cuerpo {"username": "pepe",
    // "password": "1234"},
    // el objeto Usuario tendrá los valores username="pepe" y password="1234".
}
