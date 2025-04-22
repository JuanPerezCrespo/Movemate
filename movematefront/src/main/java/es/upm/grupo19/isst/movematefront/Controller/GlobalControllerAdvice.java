package es.upm.grupo19.isst.movematefront.Controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.client.RestTemplate;

import es.upm.grupo19.isst.movematefront.Model.Usuario;

@ControllerAdvice
public class GlobalControllerAdvice {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String usuarioServiceURL;

    public GlobalControllerAdvice(@Value("${clientemanager.server}") String usuarioServiceURL) {
        this.usuarioServiceURL = usuarioServiceURL;
    }

    @ModelAttribute("usuario")
    public Usuario addUsuarioToModel() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getName())) {
            String username = authentication.getName();
            String url = usuarioServiceURL + "/myapi/usuario/" + username;
            return restTemplate.getForObject(url, Usuario.class);
        }
        return null;
    }
}