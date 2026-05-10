package services;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import models.AreaInternaModel;
import models.HospitalModel;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

/**
 * Único punto de contacto entre el frontend y los endpoints
 * de hospitales/áreas del backend.
 *
 * Todos los métodos son bloqueantes (síncronos). Las vistas los llaman
 * siempre dentro de un Task de JavaFX para no congelar la interfaz.
 */
public class HospitalService {

    private static final String BASE_URL = "http://localhost:8080";

    private final HttpClient http = HttpClient.newHttpClient();
    private final Gson gson = new Gson();

    /** GET /hospitals — lista todos los hospitales. */
    public List<HospitalModel> getAll() throws Exception {
        String json = get("/hospitals");
        return gson.fromJson(json, new TypeToken<List<HospitalModel>>() {}.getType());
    }

    /** GET /hospitals/{codigo} — obtiene un hospital por su código. */
    public HospitalModel getById(String codigo) throws Exception {
        String json = get("/hospitals/" + codigo);
        return gson.fromJson(json, HospitalModel.class);
    }

    /**
     * Crea un nuevo hospital en el sistema.
     * Mapea al endpoint POST /hospitals del backend.
     *
     * @param body datos del hospital a crear
     * @return el hospital creado con los datos asignados por el servidor
     * @throws Exception si el servidor responde con un código de error
     */
    public HospitalModel createHospital(HospitalCreateBody body) throws Exception {
        String json = post("/hospitals", gson.toJson(body));
        return gson.fromJson(json, HospitalModel.class);
    }

    /** PUT /hospitals/{codigo} — actualiza los datos de un hospital. */
    public HospitalModel update(String codigo, HospitalUpdateBody body) throws Exception {
        String json = put("/hospitals/" + codigo, gson.toJson(body));
        return gson.fromJson(json, HospitalModel.class);
    }

    /** GET /hospitals/{codigoHospital}/areas — lista las áreas de un hospital. */
    public List<AreaInternaModel> getAreas(String codigoHospital) throws Exception {
        String json = get("/hospitals/" + codigoHospital + "/areas");
        return gson.fromJson(json, new TypeToken<List<AreaInternaModel>>() {}.getType());
    }

    /** POST /hospitals/{codigoHospital}/areas — crea un área nueva en el hospital. */
    public AreaInternaModel createArea(String codigoHospital, AreaCreateBody body) throws Exception {
        String json = post("/hospitals/" + codigoHospital + "/areas", gson.toJson(body));
        return gson.fromJson(json, AreaInternaModel.class);
    }

    /** PUT /hospitals/{codigoHospital}/areas/{codigoArea} — actualiza un área. */
    public AreaInternaModel updateArea(String codigoHospital, String codigoArea,
                                       AreaUpdateBody body) throws Exception {
        String json = put("/hospitals/" + codigoHospital + "/areas/" + codigoArea,
                gson.toJson(body));
        return gson.fromJson(json, AreaInternaModel.class);
    }

    /**
     * Refleja HospitalUpdateRequest del backend.
     * Usa record para que Gson lo serialice directamente a JSON.
     */
    /**
     * Refleja HospitalRequest del backend para la creación de un hospital.
     */
    public record HospitalCreateBody(String codigo, String nombre, String direccion,
                                     String telefono, String codigoCiudad, Boolean estado) {}

    /**
     * Refleja HospitalUpdateRequest del backend para la actualización de un hospital.
     */
    public record HospitalUpdateBody(String nombre, String direccion,
                                     String telefono, String codigoCiudad,
                                     Boolean estado) {}

    /**
     * Refleja AreaInternaUpdateRequest del backend.
     */
    public record AreaUpdateBody(String nombre, String descripcion,
                                 String codigoAreaInterna) {}

    public record AreaCreateBody(String codigo, String nombre,
                                 String descripcion, String codigoAreaInterna) {}

    private String get(String path) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .GET()
                .build();
        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
        validarRespuesta(response);
        return response.body();
    }

    private String post(String path, String jsonBody) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();
        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
        validarRespuesta(response);
        return response.body();
    }

    private String put(String path, String jsonBody) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();
        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
        validarRespuesta(response);
        return response.body();
    }

    /** Lanza excepción si el servidor devuelve un código de error (4xx, 5xx). */
    private void validarRespuesta(HttpResponse<String> response) throws Exception {
        if (response.statusCode() >= 400) {
            throw new Exception("Error del servidor (" + response.statusCode() + "): " + response.body());
        }
    }
}
