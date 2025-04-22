package es.upm.grupo19.isst.movemateback.Repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import es.upm.grupo19.isst.movemateback.Model.Actividad;
import es.upm.grupo19.isst.movemateback.Model.Cliente;
import es.upm.grupo19.isst.movemateback.Model.Reserva;

public interface ReservaRepository extends CrudRepository<Reserva, Long> {
    // Aquí puedes definir métodos adicionales de consulta si es necesario
    // Por ejemplo, para encontrar una reserva por su ID de cliente:
    // List<Reserva> findByClienteId(Long clienteId);

    // Encontrar todas las reservas de una actividad
    List<Reserva> findByActividadId(Long actividadId);
    // Método para contar el numero de reservas de una actividad
    int countByActividad(Actividad actividad);
    // Eliminar todas las reservas asociadas a las actividades del monitor
    void deleteByActividadMonitorId(Long monitorId);
    // Obtenemos la reserva del usuario y la actividad.
    Reserva findByClienteAndActividad(Cliente cliente, Actividad actividad);
}
