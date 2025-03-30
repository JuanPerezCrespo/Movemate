package com.movemate.repository;

import com.movemate.model.Actividad;
import com.movemate.model.Monitor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActividadRepository extends JpaRepository<Actividad, Long> {
    List<Actividad> findByMonitor(Monitor monitor);
    List<Actividad> findByEstado(String estado);
}
