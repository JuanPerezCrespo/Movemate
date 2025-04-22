package es.upm.grupo19.isst.movemateback.Repository;

import org.springframework.data.repository.CrudRepository;

import es.upm.grupo19.isst.movemateback.Model.Usuario;

public interface UsuarioRepository extends CrudRepository<Usuario, Long> {
    // Aquí puedes definir métodos adicionales de consulta si es necesario
    // Por ejemplo, para encontrar un usuario por su email:
    // Optional<Usuario> findByEmail(String email);
    
    // Método para encontrar un usuario por su nombre de usuario:
    Usuario findByUsername(String username);
    // Método para encontrar un usuario por su email:
    Usuario findByEmail(String email);

}
