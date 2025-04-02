package com.movemate.repository;

import com.movemate.model.Reserva;
import com.movemate.model.Usuario;
import com.movemate.model.Actividad;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {
    List<Reserva> findByUsuario(Usuario usuario);
    List<Reserva> findByUsuarioAndActividad(Usuario usuario, Actividad actividad);
    boolean existsByUsuarioAndActividad(Usuario usuario, Actividad actividad);
    void deleteAllByUsuario(Usuario usuario);

}
