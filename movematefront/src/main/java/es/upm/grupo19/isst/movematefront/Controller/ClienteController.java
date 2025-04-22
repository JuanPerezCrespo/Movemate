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
    public String registrarCliente(@Validated Cliente cliente, BindingResult result) {
        cliente.setRol("cliente"); // Asignar el rol de cliente por defecto
        logger.info("Registrando nuevo cliente: " + cliente.getNombre() + " " + cliente.getApellidos());
        try {
            restTemplate.postForObject(ClienteManagerURL + "/myapi/cliente", cliente, Cliente.class);
        } catch (HttpClientErrorException e) {
            logger.info("Error al registrar el cliente: " + e.getMessage());
            String errorMesage = e.getResponseBodyAsString();
            if (errorMesage.contains("email")) {
                result.rejectValue("email", "error.cliente", "El email ya está en uso. Por favor, elige otro.");
            } else if (errorMesage.contains("El nombre de usuario ya está en uso.")) {
                result.rejectValue("username", "error.cliente",
                        "El nombre de usuario ya está en uso. Por favor, elige otro.");
            } else if (errorMesage.contains("contraseña")) {
                result.rejectValue("password", "error.cliente",
                        "La contraseña es demasiado corta. Debe tener al menos 8 caracteres.");
            } else if (errorMesage.contains("El nombre de usuario debe tener al menos 5 caracteres")) {
                result.rejectValue("username", "error.cliente",
                        "El nombre de usuario debe tener al menos 5 caracteres.");
            } else {
                result.rejectValue("email", "error.cliente", "Error al registrar el cliente: " + errorMesage);
            }
            return VISTA_REGISTRO_CLIENTES; // Redirige a la vista de registro si hay errores
        }
        return VISTA_LOGIN; // Redirige a la vista de login después del registro
    }
}
