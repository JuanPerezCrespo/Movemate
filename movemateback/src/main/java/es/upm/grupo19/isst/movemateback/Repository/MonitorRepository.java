package es.upm.grupo19.isst.movemateback.Repository;

import org.springframework.data.repository.CrudRepository;

import es.upm.grupo19.isst.movemateback.Model.Monitor;

public interface MonitorRepository extends CrudRepository<Monitor, Long> {
    // Aquí puedes definir métodos adicionales de consulta si es necesario
    // Por ejemplo, para encontrar un monitor por su nombre:
    // List<Monitor> findByNombre(String nombre);

    Monitor findByUsername(String username);
    Monitor findByEmail(String email);
}
