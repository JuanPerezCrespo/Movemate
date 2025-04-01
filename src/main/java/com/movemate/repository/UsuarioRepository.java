package com.movemate.repository;

import com.movemate.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// Repository para la entidad Usuario
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByUsername(String username);      // Método para buscar un usuario por su nombre de usuario
}

