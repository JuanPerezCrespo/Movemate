package es.upm.grupo19.isst.movematefront.Controller;

import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import es.upm.grupo19.isst.movematefront.Model.Monitor;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Controller
@RequestMapping
public class MonitorController {

    private static final Logger logger = Logger.getLogger(ClienteController.class.getName());
    public final String ClienteManagerURL;
    public static final String VISTA_LOGIN = "login";
    public static final String VISTA_REGISTRO_MONITORES = "registroMonitores";
    private RestTemplate restTemplate = new RestTemplate();

    public MonitorController(@Value("${clientemanager.server}") String clienteManagerURL) {
        this.ClienteManagerURL = clienteManagerURL;
    }

    @GetMapping("/registroMonitores")
    public String registroMonitores(Model model) {
        logger.info("Accediendo a la vista de registro");
        model.addAttribute("monitor", new Monitor()); // Asegúrate de que la clase Monitor esté disponible
        return VISTA_REGISTRO_MONITORES;
    }

    @PostMapping("/registroMonitores")
    public String registrarMonitor(@Validated Monitor monitor, BindingResult result) {
        monitor.setRol("monitor"); // Asignar el rol de monitor por defecto
        logger.info("Registrando nuevo monitor: " + monitor.getNombre() + " " + monitor.getApellidos());
        try {
            restTemplate.postForObject(ClienteManagerURL + "/myapi/monitor", monitor, Monitor.class);
        } catch (HttpClientErrorException e) {
            logger.info("Error al registrar el monitor: " + e.getMessage());
            String errorMesage = e.getResponseBodyAsString();
            if (errorMesage.contains("email")) {
                result.rejectValue("email", "error.monitor", "El email ya está en uso. Por favor, elige otro.");
            } else if (errorMesage.contains("El nombre de usuario ya está en uso.")) {
                result.rejectValue("username", "error.monitor",
                        "El nombre de usuario ya está en uso. Por favor, elige otro.");
            } else if (errorMesage.contains("contraseña")) {
                result.rejectValue("password", "error.monitor",
                        "La contraseña es demasiado corta. Debe tener al menos 8 caracteres.");
            } else if (errorMesage.contains("El nombre de usuario debe tener al menos 5 caracteres")) {
                result.rejectValue("username", "error.monitor",
                        "El nombre de usuario debe tener al menos 5 caracteres.");
            } else {
                result.rejectValue("email", "error.monitor", "Error al registrar el monitor: " + errorMesage);
            }
            return VISTA_REGISTRO_MONITORES; // Redirige a la vista de registro si hay errores
        }
        return VISTA_LOGIN; // Redirige a la vista de login después del registro
    }
}
