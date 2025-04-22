package es.upm.grupo19.isst.movemateback.Controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import es.upm.grupo19.isst.movemateback.Model.Reserva;
import es.upm.grupo19.isst.movemateback.Repository.ReservaRepository;

@CrossOrigin
@RestController
@RequestMapping("/myapi/reserva")
public class ReservaController {

    private final ReservaRepository reservaRepository;

    public static final Logger log = LoggerFactory.getLogger(ReservaController.class);

    public ReservaController(ReservaRepository reservaRepository) {
        this.reservaRepository = reservaRepository;
    }

    // Endpoint para obtener una reserva por su ID, si no existe devuelve null.
    @GetMapping("/{id}")
    public Reserva getReservaById(@PathVariable Long id) {
        return reservaRepository.findById(id).orElse(null);
    }
}
