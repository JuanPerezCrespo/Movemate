package es.upm.grupo19.isst.movematefront.Controller;

import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import es.upm.grupo19.isst.movematefront.Model.Usuario;

@Controller
@RequestMapping
public class LoginController {

    public static final String VISTA_LOGIN = "login";
    public static final String VISTA_MIS_ACTIVIDADES = "misActividades";
    public final String usuarioServiceURL;
    private static final Logger logger = Logger.getLogger(LoginController.class.getName());
    private RestTemplate restTemplate = new RestTemplate();

    public LoginController(@Value("${clientemanager.server}") String clienteManagerURL) {
        this.usuarioServiceURL = clienteManagerURL;
    }

    @GetMapping("/login")
    public String login(Model model) {
        logger.info("Accediendo a la vista de login");
        model.addAttribute("usuario", new Usuario()); // Asegúrate de que la clase Usuario esté disponible
        return VISTA_LOGIN;
    }

    @PostMapping("/usuario/eliminar")
    public String eliminarCuenta(Model model) {
        Usuario usuario = (Usuario) model.getAttribute("usuario");
        logger.info("Eliminando cuenta del usuario con ID: " + usuario.getId());
        try {
            String url = usuarioServiceURL + "/myapi/usuario/" + usuario.getId() + "/eliminar";
            restTemplate.delete(url);
            model.addAttribute("success", "Tu cuenta ha sido eliminada correctamente.");
            return VISTA_LOGIN;
        } catch (HttpClientErrorException e) {
            String errorMessage = e.getResponseBodyAsString();
            logger.severe("Error al eliminar la cuenta: " + errorMessage);
            model.addAttribute("error", "No se pudo eliminar la cuenta. Inténtalo de nuevo más tarde.");
            return VISTA_MIS_ACTIVIDADES; // Redirigir a la misma página en caso de error
        }
    }
}
