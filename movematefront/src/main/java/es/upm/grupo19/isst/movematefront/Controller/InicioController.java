package es.upm.grupo19.isst.movematefront.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.ui.Model;


@Controller
@RequestMapping
public class InicioController {

    public static final String VISTA_INICIO = "inicio";

    @GetMapping("/inicio")
    public String inicio(Model model) {
        return VISTA_INICIO;
    }
}
