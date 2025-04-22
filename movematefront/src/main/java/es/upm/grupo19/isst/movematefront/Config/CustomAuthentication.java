package es.upm.grupo19.isst.movematefront.Config;

import java.util.ArrayList;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;


import es.upm.grupo19.isst.movematefront.Controller.ClienteController;

@Component
public class CustomAuthentication implements AuthenticationProvider{

        private static final Logger logger = Logger.getLogger(ClienteController.class.getName());
        public final String ClienteManagerURL;
        private RestTemplate restTemplate = new RestTemplate();

        public CustomAuthentication(@Value("${clientemanager.server}") String clienteManagerURL) {
            this.ClienteManagerURL = clienteManagerURL;
        }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String password = authentication.getCredentials().toString();

        logger.info("Se está autenticando el usuario: " + username);

        // Crear un MultiValueMap con las credenciales
        var credenciales = new org.springframework.util.LinkedMultiValueMap<String, String>();
        credenciales.add("username", username);
        credenciales.add("password", password);

        try {
            // Configurar los headers para enviar los datos como application/x-www-form-urlencoded
            String url = ClienteManagerURL + "/myapi/usuario";
            var headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED);

            // Crear la entidad HTTP con los datos y los headers
            var request = new org.springframework.http.HttpEntity<>(credenciales, headers);

            // Realizar la petición POST al backend
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            // Verificar la respuesta del backend
            if (response.getStatusCode().is2xxSuccessful()) {
                logger.info("Autenticación exitosa para el usuario: " + username);
                return new UsernamePasswordAuthenticationToken(username, password, new ArrayList<>());
            } else {
                logger.warning("Error en la autenticación: " + response.getBody());
                throw new BadCredentialsException(response.getBody());
            }
        } catch (Exception e) {
            logger.severe("Excepción durante la autenticación: " + e.getMessage());
            throw new BadCredentialsException("Error al autenticar al usuario");
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
}
