package es.upm.grupo19.isst.movemateback.Controller;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import es.upm.grupo19.isst.movemateback.Config.GeocodingService;
import es.upm.grupo19.isst.movemateback.Model.Actividad;
import es.upm.grupo19.isst.movemateback.Model.Cliente;
import es.upm.grupo19.isst.movemateback.Model.Pago;
import es.upm.grupo19.isst.movemateback.Model.Reserva;
import es.upm.grupo19.isst.movemateback.Repository.ActividadRepository;
import es.upm.grupo19.isst.movemateback.Repository.ClienteRepository;
import es.upm.grupo19.isst.movemateback.Repository.PagoRepository;
import es.upm.grupo19.isst.movemateback.Repository.ReservaRepository;
import jakarta.transaction.Transactional;

@CrossOrigin
@RestController
@RequestMapping("/myapi/actividad")
public class ActividadController {

    private final ActividadRepository actividadRepository;
    private final ClienteRepository clienteRepository;
    private final ReservaRepository reservaRepository;
    private final PagoRepository pagoRepository;
    private final GeocodingService geocodingService;

    public static final Logger log = LoggerFactory.getLogger(ActividadController.class);

    public ActividadController(ActividadRepository actividadRepository, ClienteRepository clienteRepository,
            ReservaRepository reservaRepository, GeocodingService geocodingService, PagoRepository pagoRepository) {
        this.pagoRepository = pagoRepository;
        this.geocodingService = geocodingService;
        this.reservaRepository = reservaRepository;
        this.clienteRepository = clienteRepository;
        this.actividadRepository = actividadRepository;
    }

    // Endpoint para obtener todas las actividades.
    @GetMapping
    public Iterable<Actividad> getAllActividades() {
        return actividadRepository.findAll();
    }

    // Endpoint para obtener una actividad por su ID.
    @GetMapping("/{id}")
    public Actividad getActividadById(@PathVariable Long id) {
        return actividadRepository.findById(id).orElse(null);
    }

    // Endpoint para editar una actividad por su ID.
    @PutMapping("/{id}/editar")
    public ResponseEntity<?> editarActividad(@PathVariable Long id, @RequestBody Actividad actividad) {
        Actividad actividadExistente = actividadRepository.findById(id).orElse(null);
        if (actividadExistente != null) {
            if (actividad.getDeporte() != null) {
                actividadExistente.setDeporte(actividad.getDeporte());
            }
            if (actividad.getUbicacion() != null) {
                actividadExistente.setUbicacion(actividad.getUbicacion());
            }
            if (actividad.getDireccion() != null) {
                actividadExistente.setDireccion(actividad.getDireccion());
            }
            if (actividad.getNivel() != null) {
                actividadExistente.setNivel(actividad.getNivel());
            }
            if (actividad.getPrecio() != 0) {
                actividadExistente.setPrecio(actividad.getPrecio());
            }
            if (actividad.getDescripcion() != null) {
                actividadExistente.setDescripcion(actividad.getDescripcion());
            }
            if (actividad.getImagenUrl() != null) {
                actividadExistente.setImagenUrl(actividad.getImagenUrl());
            }
            if (actividad.getMaxParticipantes() != 0) {
                actividadExistente.setMaxParticipantes(actividad.getMaxParticipantes());
            }
            if (actividad.getFecha() != null) {
                actividadExistente.setFecha(actividad.getFecha());
            }
            if (!"Cancelada".equals(actividadExistente.getEstado())) {
                log.info("La actividad tiene estado: " + actividadExistente.getEstado());
                // Comprobamos si la actividad está llena o no con la edición de participantes:
                if (reservaRepository.countByActividad(actividadExistente) >= actividadExistente
                        .getMaxParticipantes()) {
                    actividadExistente.setEstado("Completa");
                } else {
                    actividadExistente.setEstado("Disponible");
                }
            }
            // Obtener coordenadas de la dirección si está presente
            if (actividad.getDireccion() != null && !actividad.getDireccion().isEmpty()) {
                GeocodingService.Coordenadas coords = geocodingService
                        .obtenerCoordenadas(actividad.getDireccion());
                if (coords != null) {
                    actividadExistente.setLatitud(coords.getLat());
                    actividadExistente.setLongitud(coords.getLon());
                    log.info("Coordenadas asignadas: lat=" + coords.getLat() + ", lon=" + coords.getLon());
                } else {
                    log.warn("No se pudieron obtener coordenadas para la dirección: " + actividad.getDireccion());
                }
            }
            // Guardamos la actividad editada
            actividadRepository.save(actividadExistente);
            log.info("La actividad tiene estado: " + actividadExistente.getEstado());

            return ResponseEntity.ok("Actividad editada correctamente.");
        } else {
            return ResponseEntity.badRequest().body("Actividad no encontrada.");
        }
    }

    // Endpoint para eliminar una actividad por su ID.
    @DeleteMapping("/{id}/eliminar")
    public ResponseEntity<?> eliminarActividad(@PathVariable Long id) {
        Actividad actividad = actividadRepository.findById(id).orElse(null);
        if (actividad != null) {
            borrarReservas(id);
            // Eliminar la actividad
            actividadRepository.delete(actividad);
            return ResponseEntity.ok("Actividad y sus reservas asociadas eliminadas correctamente.");
        } else {
            return ResponseEntity.badRequest().body("Actividad no encontrada.");
        }
    }

    @PostMapping("/{id}/reservar/{usuarioId}")
    public ResponseEntity<?> reservarActividad(@PathVariable Long id, @PathVariable Long usuarioId) {
        Cliente cliente = clienteRepository.findById(usuarioId).orElse(null);
        Actividad actividad = actividadRepository.findById(id).orElse(null);

        // Validar si el cliente ya está inscrito en la actividad
        boolean yaReservado = actividad.getReservas().stream()
                .anyMatch(reserva -> reserva.getCliente().getId().equals(usuarioId));

        // Validar si la actividad ya ha ocurrido
        if (actividad.getFecha().isBefore(LocalDateTime.now())) {
            return ResponseEntity.badRequest()
                    .body("Esta actividad ya ha ocurrido. No es posible inscribirse en ella.");
        }

        // Validar si la actividad está cancelada
        if ("Cancelada".equalsIgnoreCase(actividad.getEstado())) {
            return ResponseEntity.badRequest()
                    .body("Esta actividad ha sido cancelada. No es posible inscribirse en ella.");
        }

        // Validar si la actividad está llena
        if (reservaRepository.countByActividad(actividad) >= actividad.getMaxParticipantes()) {
            return ResponseEntity.badRequest()
                    .body("Esta actividad está llena. No es posible inscribirse en ella.");
        }

        if (!yaReservado) {
            // Crear una nueva reserva
            Reserva reserva = new Reserva();
            Pago pago = new Pago();
            pago.setCantidad(0.0); // Inicializar el pago a 0.0
            pago.setEstado("Pendiente"); // Estado inicial del pago
            pago.setMetodoPago("No se ha seleccionado un método de pago"); // Método de pago inicial

            // Guardar el pago primero
            pagoRepository.save(pago);

            // Asignar la reserva al pago y viceversa
            pago.setReserva(reserva);
            reserva.setCliente(cliente);
            reserva.setActividad(actividad);
            reserva.setPago(pago);

            // Guardar la reserva
            reservaRepository.save(reserva);

            // Actualizar el estado de la actividad si es necesario
            if (reservaRepository.countByActividad(actividad) >= actividad.getMaxParticipantes()) {
                actividad.setEstado("Completa");
            } else {
                actividad.setEstado("Disponible");
            }
            actividadRepository.save(actividad);

            return ResponseEntity.ok("Te has apuntado correctamente a la actividad.");
        } else {
            return ResponseEntity.badRequest().body("Ya te encuentras apuntado en la actividad.");
        }
    }

    @Transactional
    @PostMapping("/{id}/cancelar/{usuarioId}")
    public ResponseEntity<?> cancelarReserva(@PathVariable Long id, @PathVariable Long usuarioId) {
        Cliente cliente = clienteRepository.findById(usuarioId).orElse(null);
        Actividad actividad = actividadRepository.findById(id).orElse(null);

        // Validar si la actividad ya ha ocurrido
        if (actividad.getFecha().isBefore(LocalDateTime.now())) {
            return ResponseEntity.badRequest()
                    .body("Esta actividad ya ha ocurrido. No es posible cancelar la inscripción.");
        }

        // Validar si la actividad está cancelada
        if ("Cancelada".equalsIgnoreCase(actividad.getEstado())) {
            return ResponseEntity.badRequest()
                    .body("Esta actividad ha sido cancelada. No es posible cancelar la inscripción.");
        }

        // Buscar la reserva existente
        Reserva reserva = reservaRepository.findByClienteAndActividad(cliente, actividad);

        if (reserva != null) {
            // Eliminar el pago asociado a la reserva
            if (reserva.getPago() != null) {
                reserva.getPago().setReserva(null); // Desvincular el pago de la reserva
                pagoRepository.save(reserva.getPago()); // Guardar el pago actualizado
            }
            pagoRepository.delete(reserva.getPago());
            // Eliminar la reserva
            reservaRepository.delete(reserva);
            // Actualizar el estado de la actividad si es necesario
            if (reservaRepository.countByActividad(actividad) < actividad.getMaxParticipantes()) {
                actividad.setEstado("Disponible");
                actividadRepository.save(actividad);
            } else {
                actividad.setEstado("Completa");
                actividadRepository.save(actividad);
            }

            return ResponseEntity.ok("Has cancelado tu inscripción a la actividad.");
        } else {
            return ResponseEntity.badRequest().body("No tienes una reserva activa para esta actividad.");
        }
    }

    // Endpoint para cancelar una actividad por su ID.
    @Transactional
    @PostMapping("/{id}/cancelarActividad")
    public ResponseEntity<?> cancelarActividad(@PathVariable Long id) {
        Actividad actividad = actividadRepository.findById(id).orElse(null);
        // Comprobamos si la actividad ya está cancelada
        if ("Cancelada".equalsIgnoreCase(actividad.getEstado())) {
            return ResponseEntity.badRequest().body("La actividad ya está cancelada.");
        }
        // Comprobamos si la actividad ya ha ocurrido
        if (actividad.getFecha().isBefore(LocalDateTime.now())) {
            return ResponseEntity.badRequest().body("No se puede cancelar una actividad que ya ha ocurrido.");
        }
        // Borramos todas las reservas asociadas a la actividad y sus pagos
        borrarReservas(id);
        // Cambiar el estado de la actividad a "Cancelada"
        actividad.setEstado("Cancelada");
        actividadRepository.save(actividad);
        return ResponseEntity.ok("Actividad cancelada correctamente.");
    }

    // Endpoint para reabrir una actividad por su ID.
    @PostMapping("/{id}/reabrirActividad")
    public ResponseEntity<?> reabrirActividad(@PathVariable Long id) {
        Actividad actividad = actividadRepository.findById(id).orElse(null);
        // Comprobamos si la actividad ya está cancelada
        if (!"Cancelada".equalsIgnoreCase(actividad.getEstado())) {
            return ResponseEntity.badRequest().body("La actividad no está cancelada.");
        }
        if (reservaRepository.countByActividad(actividad) >= actividad.getMaxParticipantes()) {
            actividad.setEstado("Completa");
            actividadRepository.save(actividad);
        } else {
            actividad.setEstado("Disponible");
            actividadRepository.save(actividad);
        }
        actividadRepository.save(actividad);
        return ResponseEntity.ok("Actividad reabierta correctamente.");
    }

    // Endpoint que devuelve true o false si el usuario está inscrito en la
    // actividad
    @GetMapping("/{id}/reservado/{usuarioId}")
    public ResponseEntity<?> isReservado(@PathVariable Long id, @PathVariable Long usuarioId) {
        Cliente cliente = clienteRepository.findById(usuarioId).orElse(null);
        Actividad actividad = actividadRepository.findById(id).orElse(null);

        if (cliente == null || actividad == null) {
            return ResponseEntity.badRequest().body("Cliente o actividad no encontrados.");
        }

        // Verificar si el cliente ya está inscrito en la actividad
        boolean yaReservado = actividad.getReservas().stream()
                .anyMatch(reserva -> reserva.getCliente().getId().equals(usuarioId));

        return ResponseEntity.ok(yaReservado);
    }

    // Devuelve todas las actividades de un cliente
    @GetMapping("/misActividades/{usuarioId}")
    public Iterable<Actividad> getActividadesByClienteId(@PathVariable Long usuarioId) {
        Cliente cliente = clienteRepository.findById(usuarioId).orElse(null);
        if (cliente != null) {
            return actividadRepository.findAllByReservasCliente(cliente);
        } else {
            return null;
        }
    }

    // Este método elimina todos los pagos, reservas de una actividad pero no la
    // actividad.
    @Transactional
    @DeleteMapping("{id}/borrarReservas")
    public ResponseEntity<?> borrarReservas(@PathVariable Long id) {
        Actividad actividad = actividadRepository.findById(id).orElse(null);

        if (actividad == null) {
            return ResponseEntity.badRequest().body("Actividad no encontrada.");
        }

        // Iterar sobre las reservas asociadas a la actividad
        List<Reserva> reservas = actividad.getReservas();
        if (reservas != null && !reservas.isEmpty()) {
            for (Reserva reserva : reservas) {
                if (reserva.getPago() != null) {
                    // Eliminar el pago asociado a la reserva
                    Pago pago = reserva.getPago();
                    reserva.setPago(null); // Desvincular el pago de la reserva
                    reservaRepository.save(reserva); // Guardar la reserva actualizada
                    pagoRepository.delete(pago); // Eliminar el pago
                }
            }
            // Eliminar todas las reservas asociadas
            reservaRepository.deleteAll(reservas);
            return ResponseEntity.ok("Reservas asociadas eliminadas correctamente.");

        } else {
            return ResponseEntity.ok("No hay reservas asociadas a esta actividad.");
        }

    }
}
