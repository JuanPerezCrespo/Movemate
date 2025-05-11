package es.upm.grupo19.isst.movematefront.Controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import es.upm.grupo19.isst.movematefront.Model.Actividad;
import es.upm.grupo19.isst.movematefront.Model.Pago;
import es.upm.grupo19.isst.movematefront.Model.Usuario;

@Controller
@RequestMapping("/actividad")
public class ActividadController {

    private static final Logger logger = Logger.getLogger(ActividadController.class.getName());
    public final String usuarioServiceURL;
    public static final String VISTA_INICIO = "inicio";
    public static final String VISTA_CREAR_ACTIVIDAD = "crearActividad";
    public static final String VISTA_MIS_ACTIVIDADES = "misActividades";
    public static final String VISTA_DETALLE_ACTIVIDAD = "detalleActividad";
    public static final String VISTA_EDITAR_ACTIVIDAD = "editarActividad";
    public static final String VISTA_ACTIVIDADES = "actividades";

    private RestTemplate restTemplate = new RestTemplate();

    public ActividadController(@Value("${clientemanager.server}") String clienteManagerURL) {
        this.usuarioServiceURL = clienteManagerURL;
    }

    @GetMapping("/crearActividad")
    public String crearActividad(Model model) {
        logger.info("Accediendo a la vista de crear actividad");
        model.addAttribute("actividad", new Actividad());
        return VISTA_CREAR_ACTIVIDAD;
    }

    @PostMapping("/guardar")
    public String guardarActividad(Actividad actividad, BindingResult result,
            @RequestParam("imagen") MultipartFile imagen,
            @RequestParam("monitorID") Long usuarioId, Model model) {
        logger.info("Guardando nueva actividad: " + actividad.getDeporte());
        logger.info("Id del usuario que inicia sesión: " + usuarioId);

        // Validar errores en el formulario
        if (result.hasErrors()) {
            logger.info("Errores en el formulario: " + result.getAllErrors());
            model.addAttribute("error", "Hay errores en el formulario. Por favor, corrígelos.");
            return VISTA_CREAR_ACTIVIDAD;
        }

        // Guardar imagen en carpeta estática
        actividad.setImagenUrl(guardarImagen(imagen));
        actividad.setEstado("Disponible");

        try {
            // Enviar el ID del usuario como parámetro en la solicitud al servicio REST
            String url = usuarioServiceURL + "/myapi/monitor/" + usuarioId + "/actividades";
            restTemplate.postForObject(url, actividad, String.class);
        } catch (HttpClientErrorException e) {
            String errorMessage = e.getResponseBodyAsString();
            logger.info("Error al reservar la actividad: " + errorMessage);
            model.addAttribute("error", errorMessage);
            return VISTA_CREAR_ACTIVIDAD;
        }
        // Obtenemos la lista de actividades después de crear una actividad
        List<Actividad> actividades = getActividades();
        model.addAttribute("actividades", actividades);
        return VISTA_ACTIVIDADES; // Redirigir a la vista de inicio después de guardar
    }

    @GetMapping("/misActividades")
    public String misActividades(@RequestParam("userId") Long userId, Model model) {
        logger.info("Accediendo a la vista de mis actividades con ID: " + userId);

        // El objeto "usuario" ya está disponible en el modelo gracias a
        // GlobalControllerAdvice
        Usuario usuario = (Usuario) model.getAttribute("usuario");

        if (usuario != null && "monitor".equals(usuario.getRol())) {
            logger.info("Accediendo a la vista de mis actividades del monitor con ID: " + userId);
            String url = usuarioServiceURL + "/myapi/monitor/" + userId + "/actividades";
            ResponseEntity<List<Actividad>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<Actividad>>() {
                    });
            List<Actividad> actividades = response.getBody();
            model.addAttribute("actividades", actividades);
            return VISTA_MIS_ACTIVIDADES;
        } else if (usuario != null) {
            logger.info("Accediendo a la vista de mis actividades del usuario con ID: " + userId);
            String url = usuarioServiceURL + "/myapi/actividad/misActividades/" + userId;
            ResponseEntity<List<Actividad>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<Actividad>>() {
                    });
            List<Actividad> actividades = response.getBody();
            model.addAttribute("actividades", actividades);
            return VISTA_MIS_ACTIVIDADES;
        }

        // Si el usuario no está autenticado o no tiene rol, redirigir a inicio
        return VISTA_INICIO;

    }

    // Metodo para ver en detalle una actividad.
    @GetMapping("/{id}")
    public String verActividad(@PathVariable Long id, Model model) {
        verificaciones(model, id);
        return VISTA_DETALLE_ACTIVIDAD; // Nombre de la vista que muestra los detalles de la actividad
    }

    // Metodo para reservar una actividad.
    @PostMapping("/{id}/reservar")
    public String reservarActividad(@PathVariable Long id, @RequestParam("usuarioId") Long usuarioId, Model model) {
        logger.info("Reservando actividad con ID: " + id + " para el usuario con ID: " + usuarioId);
        try {
            String url = usuarioServiceURL + "/myapi/actividad/" + id + "/reservar/" + usuarioId;
            String response = restTemplate.postForObject(url, null, String.class);
            logger.info("Respuesta del servicio: " + response);
            model.addAttribute("success", "Te has apuntado correctamente a la actividad.");
        } catch (HttpClientErrorException e) {
            String errorMessage = e.getResponseBodyAsString();
            logger.info("Error al reservar la actividad: " + errorMessage);
            model.addAttribute("error", errorMessage);
        }
        // Recuperar los detalles de la actividad para asegurarse de que el modelo esté
        verificaciones(model, id);
        return VISTA_DETALLE_ACTIVIDAD; // Redirigir a la vista de detalle de la actividad
    }

    // Metodo para cancelar una reserva.
    @PostMapping("/{id}/cancelar")
    public String cancelarReserva(@PathVariable Long id, @RequestParam("usuarioId") Long usuarioId, Model model) {
        logger.info("Cancelando reserva de actividad con ID: " + id + " para el usuario con ID: " + usuarioId);
        try {
            String url = usuarioServiceURL + "/myapi/actividad/" + id + "/cancelar/" + usuarioId;
            String response = restTemplate.postForObject(url, null, String.class);
            logger.info("Respuesta del servicio: " + response);
            model.addAttribute("success", "Has cancelado correctamente la reserva.");
        } catch (HttpClientErrorException e) {
            String errorMessage = e.getResponseBodyAsString();
            logger.info("Error al cancelar la reserva: " + errorMessage);
            model.addAttribute("error", errorMessage);
        }
        // Recuperar los detalles de la actividad para asegurarse de que el modelo esté
        verificaciones(model, id);
        return VISTA_DETALLE_ACTIVIDAD; // Redirigir a la vista de detalle de la actividad
    }

    // Vista para editar una actividad.
    @GetMapping("{id}/editar")
    public String editarActividad(@PathVariable Long id, Model model) {
        logger.info("Accediendo a la vista de editar actividad con ID: " + id);
        String url = usuarioServiceURL + "/myapi/actividad/" + id;
        Actividad actividad = restTemplate.getForObject(url, Actividad.class);
        model.addAttribute("actividad", actividad);
        return VISTA_EDITAR_ACTIVIDAD; // Nombre de la vista para editar la actividad
    }

    // Metodo para editar una actividad.
    @PostMapping("{id}/editar")
    public String editarActividad(@PathVariable Long id, Actividad actividad, BindingResult result,
            @RequestParam("imagen") MultipartFile imagen, Model model) {
        logger.info("Editando actividad con ID: " + id);
        // Validar errores en el formulario
        if (result.hasErrors()) {
            logger.info("Errores en el formulario: " + result.getAllErrors());
            model.addAttribute("error", "Hay errores en el formulario. Por favor, corrígelos.");
            return VISTA_CREAR_ACTIVIDAD;
        }
        // Verificamos si la actividad no tiene imagen:
        if (imagen != null && !imagen.isEmpty()) {
            actividad.setImagenUrl(guardarImagen(imagen));
        }
        try {
            String url = usuarioServiceURL + "/myapi/actividad/" + id + "/editar";
            restTemplate.put(url, actividad);
        } catch (Exception e) {
            logger.info("Error al editar la actividad: " + e.getMessage());
            model.addAttribute("error", "No se pudo editar la actividad. Inténtalo de nuevo.");
            return VISTA_CREAR_ACTIVIDAD;
        }
        verificaciones(model, id);
        return VISTA_DETALLE_ACTIVIDAD;
    }

    // Método para cancelar una actividad.
    @PostMapping("{id}/cancelarActividad")
    public String cancelarActividad(@PathVariable Long id, Model model) {
        logger.info("Cancelando actividad con ID: " + id);
        try {
            String url = usuarioServiceURL + "/myapi/actividad/" + id + "/cancelarActividad";
            restTemplate.postForObject(url, null, String.class);
            logger.info("Actividad cancelada con éxito.");
            model.addAttribute("success", "Actividad cancelada correctamente.");
        } catch (HttpClientErrorException e) {
            String errorMessage = e.getResponseBodyAsString();
            model.addAttribute("error", errorMessage);
        }
        verificaciones(model, id);
        return VISTA_DETALLE_ACTIVIDAD; // Redirigir a la vista de detalle de la actividad
    }

    // Método para reabrir una actividad.
    @PostMapping("{id}/reabrir")
    public String reabrirActividad(@PathVariable Long id, Model model) {
        logger.info("Reabriendo actividad con ID: " + id);
        try {
            String url = usuarioServiceURL + "/myapi/actividad/" + id + "/reabrirActividad";
            restTemplate.postForObject(url, null, String.class);
            logger.info("Actividad reabierta con éxito.");
            model.addAttribute("success", "Actividad reabierta correctamente.");
        } catch (HttpClientErrorException e) {
            String errorMessage = e.getResponseBodyAsString();
            model.addAttribute("error", errorMessage);
        }
        verificaciones(model, id);
        return VISTA_DETALLE_ACTIVIDAD; // Redirigir a la vista de detalle de la actividad
    }

    // Metodo para eliminar una actividad.
    @PostMapping("{id}/eliminar")
    public String eliminarActividad(@PathVariable Long id, Model model) {
        logger.info("Eliminando actividad con ID: " + id);
        try {
            String url = usuarioServiceURL + "/myapi/actividad/" + id + "/eliminar";
            restTemplate.delete(url);
            logger.info("Actividad eliminada con éxito.");
            model.addAttribute("success", "Actividad eliminada correctamente.");
        } catch (Exception e) {
            logger.info("Error al eliminar la actividad: " + e.getMessage());
            model.addAttribute("error", "No se pudo eliminar la actividad. Inténtalo de nuevo.");
        }
        // Obtenemos la lista de actividades después de eliminar
        List<Actividad> actividades = getActividades();
        model.addAttribute("actividades", actividades);
        return VISTA_ACTIVIDADES;
    }

    // Metodo para listar actividades con filtros.
    @GetMapping("/actividades")
    public String listarActividades(
            @RequestParam(required = false) String mostrar, // Filtro por tiempo
            @RequestParam(required = false) Double minPrice, // Filtro por precio mínimo
            @RequestParam(required = false) Double maxPrice, // Filtro por precio máximo
            @RequestParam(required = false) String ubicacion, // Filtro por ubicación
            @RequestParam(required = false) String deporte, // Filtro por deporte
            @RequestParam(required = false) String nivel, // Filtro por nivel
            Model model) {
        LocalDateTime fechaActual = LocalDateTime.now();
        // Obtener la lista de actividades desde el servicio REST
        List<Actividad> actividades = getActividades();
        // Aplicar filtros acumulativos
        List<Actividad> actividadesFiltradas = actividades.stream()
                .filter(a -> {
                    if ("futuras".equalsIgnoreCase(mostrar)) {
                        return a.getFecha().isAfter(fechaActual); // Filtrar actividades futuras
                    } else if ("pasadas".equalsIgnoreCase(mostrar)) {
                        return a.getFecha().isBefore(fechaActual); // Filtrar actividades pasadas
                    }
                    return true; // Mostrar todas las actividades si no se especifica filtro
                })
                .filter(a -> minPrice == null || a.getPrecio() >= minPrice) // Filtrar por precio mínimo
                .filter(a -> maxPrice == null || a.getPrecio() <= maxPrice) // Filtrar por precio máximo
                .filter(a -> ubicacion == null || ubicacion.isEmpty() || ubicacion.equalsIgnoreCase(a.getUbicacion())) // Filtrar
                                                                                                                       // por
                                                                                                                       // ubicación
                .filter(a -> deporte == null || deporte.isEmpty() || deporte.equalsIgnoreCase(a.getDeporte())) // Filtrar
                                                                                                               // por
                                                                                                               // deporte
                .filter(a -> nivel == null || nivel.isEmpty() || nivel.equalsIgnoreCase(a.getNivel())) // Filtrar por
                                                                                                       // nivel
                .toList();

        // Pasar los filtros y actividades al modelo
        model.addAttribute("actividades", actividadesFiltradas);
        model.addAttribute("mostrar", mostrar);
        model.addAttribute("ubicacion", ubicacion);
        model.addAttribute("deporte", deporte);
        model.addAttribute("nivel", nivel); // Pasar el nivel seleccionado al modelo
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("fechaActual", fechaActual);

        Usuario usuario = (Usuario) model.getAttribute("usuario");
        model.addAttribute("usuario", usuario); // Agregar el objeto usuario al modelo

        return VISTA_ACTIVIDADES; // Nombre de la vista que muestra las actividades
    }

    // Método para pagar una activiadad como cliente.
    @GetMapping("{id}/pagar")
    public String pagarActividad(@PathVariable Long id, Model model) {
        logger.info("Accediendo a la vista de pago para la actividad con ID: " + id);
        String url = usuarioServiceURL + "/myapi/actividad/" + id;
        Actividad actividad = restTemplate.getForObject(url, Actividad.class);
        model.addAttribute("actividad", actividad);
        model.addAttribute("pago", new Pago()); // Agregar un nuevo objeto Pago al modelo
        return "pagarActividad"; // Nombre de la vista para el pago
    }

    @PostMapping("/pagar")
    public String procesarPago(@RequestParam("actividadId") Long actividadId,
            Pago pago, Model model) {
        Usuario usuario = (Usuario) model.getAttribute("usuario");
        Long usuarioId = usuario.getId(); // Obtener el ID del usuario desde el modelo
        logger.info("Procesando pago para la actividad con ID: " + actividadId + " y usuario con ID: " + usuarioId);
        try {
            String url = usuarioServiceURL + "/myapi/pago/" + actividadId + "/" + usuarioId;
            String response = restTemplate.postForObject(url, pago, String.class); // Cambiar a String
            logger.info("Respuesta del servicio: " + response);
            model.addAttribute("success", response); // Mostrar el mensaje del backend como éxito
        } catch (HttpClientErrorException e) {
            String errorMessage = e.getResponseBodyAsString();
            logger.info("Error al procesar el pago: " + errorMessage);
            model.addAttribute("error", errorMessage);
        }
        verificaciones(model, actividadId);
        return VISTA_DETALLE_ACTIVIDAD;
    }

    // Método para obtener todas las actividades.
    public List<Actividad> getActividades() {
        String url = usuarioServiceURL + "/myapi/actividad";
        ResponseEntity<List<Actividad>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Actividad>>() {
                });
        return response.getBody();
    }

    // Método para guardar la imagen en el servidor
    private String guardarImagen(MultipartFile imagen) {
        logger.info("Guardando imagen: " + imagen.getOriginalFilename());
        if (imagen.isEmpty()) {
            return "/Images/default.jpg";
        }
        logger.info("Guardando imagen en la carpeta uploads/");
        String uploadsDir = "src/main/resources/static/uploads/";
        File uploadsFolder = new File(uploadsDir);
        if (!uploadsFolder.exists()) {
            uploadsFolder.mkdirs();
        }

        String filename = System.currentTimeMillis() + "_" + imagen.getOriginalFilename();
        String path = uploadsDir + filename;

        try {
            Files.copy(imagen.getInputStream(), Paths.get(path), StandardCopyOption.REPLACE_EXISTING);
            return "/uploads/" + filename;
        } catch (IOException e) {
            e.printStackTrace();
            return "/Images/default.jpg";
        }
    }

    // Método verificaciones
    public String verificaciones(Model model, Long id) {
        // Recuperar los detalles de la actividad para asegurarse de que el modelo esté
        // completo
        String detalleUrl = usuarioServiceURL + "/myapi/actividad/" + id;
        Actividad actividad = restTemplate.getForObject(detalleUrl, Actividad.class);
        model.addAttribute("actividad", actividad);
        logger.info("Detalles de la actividad recuperados: " + actividad);

        // Verificar el estado de la reserva
        Usuario usuario = (Usuario) model.getAttribute("usuario");
        String url2 = usuarioServiceURL + "/myapi/actividad/" + id + "/reservado/" + usuario.getId();
        logger.info("El usuario tiene el rol: " + usuario.getRol());
        try {
            if (usuario.getRol().equals("cliente")) {
                logger.info("Verificando el estado de la reserva para la actividad con ID: " + id);
                Boolean reservado = restTemplate.getForObject(url2, Boolean.class);
                logger.info("Estado de reserva: " + reservado);
                model.addAttribute("reservado", reservado);
            }
        } catch (HttpClientErrorException e) {
            logger.info("Error al verificar la reserva: " + e.getMessage());
            model.addAttribute("error", "No se pudo verificar el estado de la reserva.");
        }

        // Verificar si la actividad ya ha pasado
        LocalDateTime fechaActual = LocalDateTime.now();
        Boolean actividadPasada = actividad.getFecha().isBefore(fechaActual);
        model.addAttribute("actividadPasada", actividadPasada);

        // Verificar si la actividad está cancelada
        Boolean actividadCancelada = actividad.getEstado().equals("Cancelada");
        model.addAttribute("actividadCancelada", actividadCancelada);

        return null; // No se necesita redirigir a otra vista aquí
    }
}
