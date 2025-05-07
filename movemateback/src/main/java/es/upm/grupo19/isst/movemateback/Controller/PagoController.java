package es.upm.grupo19.isst.movemateback.Controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import es.upm.grupo19.isst.movemateback.Model.Actividad;
import es.upm.grupo19.isst.movemateback.Model.Cliente;
import es.upm.grupo19.isst.movemateback.Model.Pago;
import es.upm.grupo19.isst.movemateback.Model.Reserva;
import es.upm.grupo19.isst.movemateback.Repository.ActividadRepository;
import es.upm.grupo19.isst.movemateback.Repository.ClienteRepository;
import es.upm.grupo19.isst.movemateback.Repository.PagoRepository;
import es.upm.grupo19.isst.movemateback.Repository.ReservaRepository;

@CrossOrigin
@RestController
@RequestMapping("/myapi/pago")
public class PagoController {

    private final PagoRepository pagoRepository;
    private final ReservaRepository reservaRepository;
    private final ActividadRepository actividadRepository;
    private final ClienteRepository clienteRepository;

    public static final Logger log = LoggerFactory.getLogger(MonitorController.class);

    public PagoController(PagoRepository pagoRepository, ReservaRepository reservaRepository,
            ActividadRepository actividadRepository, ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
        this.pagoRepository = pagoRepository;
        this.reservaRepository = reservaRepository;
        this.actividadRepository = actividadRepository;
    }
    // Endpoint para obtener todos los pagos.
    @GetMapping()
    public Iterable<Pago> getAllPagos() {
        return pagoRepository.findAll();
    }
    // Metodo para pagar una reserva de actividad por parte de un cliente.
    @PostMapping("/{actividadId}/{clienteId}")
    public ResponseEntity<?> pagarReserva(@PathVariable Long actividadId, @PathVariable Long clienteId,
            @RequestBody Pago nuevoPago) {
        // Buscar la actividad
        Actividad actividad = actividadRepository.findById(actividadId).orElse(null);
        Cliente cliente = clienteRepository.findById(clienteId).orElse(null);
        if (actividad == null) {
            return ResponseEntity.badRequest().body("La actividad no existe.");
        }
        if (cliente == null) {
            return ResponseEntity.badRequest().body("El cliente no existe.");
        }
        // Buscar la reserva del cliente para esta actividad
        Reserva reserva = reservaRepository.findByClienteAndActividad(cliente, actividad);
        if (reserva == null) {
            return ResponseEntity.badRequest().body("No tienes una reserva en esta actividad.");
        }

        // Verificar si ya existe un pago asociado a la reserva
        Pago pagoExistente = reserva.getPago();
        if (pagoExistente.getEstado().equals("Completado")) {
            return ResponseEntity.badRequest().body("Ya has pagado esta reserva.");
        } else {
            // Actualizar el pago existente
            pagoExistente.setCantidad(actividad.getPrecio()); // Actualizar la cantidad del pago
            pagoExistente.setEstado("Completado"); // Cambiar el estado a "Completado"
            pagoExistente.setMetodoPago(nuevoPago.getMetodoPago());
            pagoRepository.save(pagoExistente);
        }
        return ResponseEntity.ok("Pago procesado correctamente.");
    }

    // Metodo para eliminar un pago de una reserva de actividad por parte de un
    // cliente.
    @DeleteMapping("/{pagoId}")
    public String eliminarPago(@PathVariable Long pagoId) {
        // Comprobamos que el pago existe.
        if (pagoRepository.existsById(pagoId)) {
            // Eliminamos el pago de la base de datos.
            pagoRepository.deleteById(pagoId);
            log.info("Pago eliminado con éxito: " + pagoId);
            return "Pago eliminado con éxito: " + pagoId;
        } else {
            log.error("No se encontró el pago: " + pagoId);
            return "No se encontró el pago: " + pagoId;
        }
    }
}
