package com.movemate.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.json.JSONArray;
import org.json.JSONObject;

@Service
public class GeocodingService {

    private static final String OSM_URL = "https://nominatim.openstreetmap.org/search?format=json&q=";

    public double[] obtenerCoordenadas(String direccion) {
        String url = OSM_URL + direccion.replace(" ", "+");

        RestTemplate restTemplate = new RestTemplate();
        String response = restTemplate.getForObject(url, String.class);

        JSONArray json = new JSONArray(response);
        if (json.length() > 0) {
            JSONObject location = json.getJSONObject(0);
            double lat = location.getDouble("lat");
            double lon = location.getDouble("lon");
            return new double[]{lat, lon};
        }

        throw new RuntimeException("No se encontraron coordenadas para la dirección.");
    }
}
