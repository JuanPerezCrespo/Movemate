package es.upm.grupo19.isst.movemateback.Repository;

import org.springframework.data.repository.CrudRepository;

import es.upm.grupo19.isst.movemateback.Model.Cliente;

public interface ClienteRepository extends CrudRepository<Cliente, Long> {
    // Aquí puedes definir métodos adicionales de consulta si es necesario
    // Por ejemplo, para encontrar un cliente por su email:
    // Optional<Cliente> findByEmail(String email);

    // Método para encontrar un cliente por su nombre de usuario:
    Cliente findByUsername(String username);
    // Método para encontrar un cliente por su email:
    Cliente findByEmail(String email);

}
