package es.upm.grupo19.isst.movemateback.Controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
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

    // Metodo para pagar una reserva de actividad por parte de un cliente.
    @PostMapping("/{actividadId}/{usuarioId}")
    public String pagarReserva(@PathVariable Long actividadId, @PathVariable Long usuarioId, @RequestBody Pago pago) {
        // Obtener la actividad y el usuario por sus IDs.
        Actividad actividad = actividadRepository.findById(actividadId).orElse(null);
        Cliente cliente = clienteRepository.findById(usuarioId).orElse(null);

        // Validar que la actividad y el cliente existan.
        if (actividad == null || cliente == null) {
            log.error(
                    "Actividad o cliente no encontrados. Actividad ID: " + actividadId + ", Cliente ID: " + usuarioId);
            return "Actividad o cliente no encontrados.";
        }

        // Obtenemos la reserva del usuario y la actividad.
        Reserva reserva = reservaRepository.findByClienteAndActividad(cliente, actividad);

        // Comprobamos que la reserva existe.
        if (reserva != null) {
            Pago pagoExistente = reserva.getPago();
            if (pagoExistente != null) {
                if ("Completado".equals(pagoExistente.getEstado())) {
                    log.error("El pago ya ha sido realizado para la reserva: " + reserva.getId());
                    return "El pago ya ha sido realizado para la reserva.";
                }
            }

            if ("Cancelada".equals(reserva.getActividad().getEstado())) {
                log.error("La actividad ha sido cancelada: " + reserva.getActividad().getId());
                return "La actividad ha sido cancelada. No se puede realizar el pago.";
            }

            if (pagoExistente != null) {
                pagoExistente.setEstado(pago.getEstado());
                pagoRepository.save(pagoExistente);
                log.info("Pago actualizado para la reserva: " + reserva.getId());
                return "Pago actualizado con éxito para la reserva: " + reserva.getId();
            } else {
                reserva.setPago(pago);
                pago.setReserva(reserva);
                pagoRepository.save(pago);
                log.info("Nuevo pago creado para la reserva: " + reserva.getId());
                return "Pago realizado con éxito para la reserva: " + reserva.getId();
            }
        } else {
            log.error("No se encontró la reserva para el usuario: " + usuarioId + " y actividad: " + actividadId);
            return "No se encontró la reserva para el usuario y la actividad.";
        }
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
