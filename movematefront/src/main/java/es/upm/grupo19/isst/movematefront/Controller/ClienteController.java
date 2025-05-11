package es.upm.grupo19.isst.movematefront.Controller;

import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import es.upm.grupo19.isst.movematefront.Model.Cliente;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Controller
@RequestMapping
public class ClienteController {

    private static final Logger logger = Logger.getLogger(ClienteController.class.getName());
    public final String ClienteManagerURL;
    public static final String VISTA_LOGIN = "login";
    public static final String VISTA_REGISTRO_CLIENTES = "registroClientes";
    private RestTemplate restTemplate = new RestTemplate();

    public ClienteController(@Value("${clientemanager.server}") String clienteManagerURL) {
        this.ClienteManagerURL = clienteManagerURL;
    }

    @GetMapping("/registroClientes")
    public String registroClientes(Model model) {
        logger.info("Accediendo a la vista de registro");
        model.addAttribute("cliente", new Cliente()); // Asegúrate de que la clase Cliente esté disponible
        return VISTA_REGISTRO_CLIENTES;
    }

    @PostMapping("/registroClientes")
    public String registrarCliente(Cliente cliente, BindingResult result, Model model) {
        cliente.setRol("cliente"); // Asignar el rol de cliente por defecto
        logger.info("Registrando nuevo cliente: " + cliente.getNombre() + " " + cliente.getApellidos());
        try {
            restTemplate.postForObject(ClienteManagerURL + "/myapi/cliente", cliente, Cliente.class);
        } catch (HttpClientErrorException e) {
            String errorMessage = e.getResponseBodyAsString();
            logger.info("Error al reservar la actividad: " + errorMessage);
            model.addAttribute("error", errorMessage);
            return VISTA_REGISTRO_CLIENTES; // Redirige a la vista de registro si hay errores
        }
        return VISTA_LOGIN; // Redirige a la vista de login después del registro
    }
}
