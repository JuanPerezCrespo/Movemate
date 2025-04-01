package com.movemate.service;

import org.springframework.stereotype.Service;
import com.movemate.model.Actividad;
import com.movemate.repository.ActividadRepository;
import java.util.List;

@Service
public class ActividadService {
    private final ActividadRepository actividadRepository;
    private final GeocodingService geocodingService;

    public ActividadService(ActividadRepository actividadRepository, GeocodingService geocodingService) {
        this.actividadRepository = actividadRepository;
        this.geocodingService = geocodingService;
    }

    public List<Actividad> obtenerTodasLasActividades() {
        return actividadRepository.findAll();
    }

    public void guardarActividad(Actividad actividad) {
        if (actividad.getDireccion() != null && !actividad.getDireccion().isEmpty()) {
            try {
                double[] coordenadas = geocodingService.obtenerCoordenadas(actividad.getDireccion());
                actividad.setLatitud(coordenadas[0]);
                actividad.setLongitud(coordenadas[1]);
            } catch (Exception e) {
                System.out.println("⚠️ Error obteniendo coordenadas: " + e.getMessage());
                actividad.setLatitud(null);
                actividad.setLongitud(null);
            }
        }
        actividadRepository.save(actividad);
    }
}
