package es.upm.grupo19.isst.movemateback.Repository;

import org.springframework.data.repository.CrudRepository;

import es.upm.grupo19.isst.movemateback.Model.Pago;

public interface PagoRepository  extends CrudRepository<Pago, Long> {
    // Aquí puedes definir métodos adicionales de consulta si es necesario
    // Por ejemplo, para encontrar un pago por su ID de cliente:
    // List<Pago> findByClienteId(Long clienteId);

}
