package es.upm.grupo19.isst.movemateback.Config;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class GeocodingService {

    public static class Coordenadas {
        private String lat;
        private String lon;

        public String getLat() {
            return lat;
        }

        public void setLat(String lat) {
            this.lat = lat;
        }

        public String getLon() {
            return lon;
        }

        public void setLon(String lon) {
            this.lon = lon;
        }
    }

    public Coordenadas obtenerCoordenadas(String direccion) {
        RestTemplate restTemplate = new RestTemplate();

        String url = UriComponentsBuilder
                .fromUriString("https://nominatim.openstreetmap.org/search")
                .queryParam("q", direccion)
                .queryParam("format", "json")
                .queryParam("limit", "1")
                .build()
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "MoveMateApp/1.0 (tucorreo@ejemplo.com)");

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<Coordenadas[]> response = restTemplate.exchange(url, HttpMethod.GET, entity,
                Coordenadas[].class);

        Coordenadas[] resultados = response.getBody();
        return (resultados != null && resultados.length > 0) ? resultados[0] : null;
    }
}