package es.upm.grupo19.isst.movemateback.Repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import es.upm.grupo19.isst.movemateback.Model.Actividad;
import es.upm.grupo19.isst.movemateback.Model.Cliente;

public interface ActividadRepository extends CrudRepository<Actividad, Long> {
    // Aquí puedes definir métodos adicionales de consulta si es necesario
    // Por ejemplo, para encontrar una actividad por su nombre:
    // List<Actividad> findByNombre(String nombre);

    List<Actividad> findByDeporte(String deporte);

    // Funcion para obtener las actividades creadas por un monitor
    List<Actividad> findByMonitorId(Long monitorId);

    // Funcion para obtener todas las actividades de un cliente
    List<Actividad> findAllByReservasCliente(Cliente cliente);
    // Eliminar todas las actividades asociadas al monitor
    void deleteByMonitorId(Long monitorId);
}
