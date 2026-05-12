package services;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import models.AreaInternaModel;
import models.HospitalModel;
import views.hospital.CiudadHospitalModel;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

/**
 * Único punto de contacto entre el frontend y los endpoints
 * de hospitales y áreas del backend.
 *
 * Todos los métodos son bloqueantes (síncronos). Las vistas los llaman
 * siempre dentro de un Task de JavaFX para no congelar la interfaz.
 */
public class HospitalService {

    private static final String BASE_URL = "http://localhost:8080";

    private final HttpClient http = HttpClient.newHttpClient();
    private final Gson gson = new Gson();

    /**
     * Lista todos los hospitales registrados en el sistema.
     *
     * @return Lista de hospitales obtenida del servidor.
     * @throws Exception si el servidor responde con un código de error.
     */
    public List<HospitalModel> getAll() throws Exception {
        String json = get("/hospitals");
        return gson.fromJson(json, new TypeToken<List<HospitalModel>>() {}.getType());
    }

    /**
     * Obtiene la información completa de un hospital por su código.
     *
     * @param codigo Código único del hospital.
     * @return Hospital encontrado.
     * @throws Exception si el hospital no existe o el servidor falla.
     */
    public HospitalModel getById(String codigo) throws Exception {
        String json = get("/hospitals/" + codigo);
        return gson.fromJson(json, HospitalModel.class);
    }

    /**
     * Crea un nuevo hospital en el sistema.
     *
     * @param body Datos del hospital a crear.
     * @return Hospital creado con los datos asignados por el servidor.
     * @throws Exception si el servidor responde con un código de error.
     */
    public HospitalModel createHospital(HospitalCreateBody body) throws Exception {
        String json = post("/hospitals", gson.toJson(body));
        return gson.fromJson(json, HospitalModel.class);
    }

    /**
     * Actualiza los datos de un hospital existente.
     *
     * @param codigo Código del hospital a actualizar.
     * @param body   Nuevos datos del hospital.
     * @return Hospital actualizado.
     * @throws Exception si el servidor responde con un código de error.
     */
    public HospitalModel update(String codigo, HospitalUpdateBody body) throws Exception {
        String json = put("/hospitals/" + codigo, gson.toJson(body));
        return gson.fromJson(json, HospitalModel.class);
    }

    /**
     * Lista todas las ciudades disponibles para asociar a un hospital.
     *
     * @return Lista de ciudades obtenida del servidor.
     * @throws Exception si el servidor responde con un código de error.
     */
    public List<CiudadHospitalModel> getCiudades() throws Exception {
        String json = get("/cities");
        return gson.fromJson(json, new TypeToken<List<CiudadHospitalModel>>() {}.getType());
    }

    /**
     * Lista las áreas internas de un hospital específico.
     *
     * @param codigoHospital Código del hospital.
     * @return Lista de áreas internas del hospital.
     * @throws Exception si el servidor responde con un código de error.
     */
    public List<AreaInternaModel> getAreas(String codigoHospital) throws Exception {
        String json = get("/hospitals/" + codigoHospital + "/areas");
        return gson.fromJson(json, new TypeToken<List<AreaInternaModel>>() {}.getType());
    }

    /**
     * Crea una nueva área interna en el hospital indicado.
     *
     * @param codigoHospital Código del hospital donde se crea el área.
     * @param body           Datos del área a crear.
     * @return Área creada con los datos asignados por el servidor.
     * @throws Exception si el servidor responde con un código de error.
     */
    public AreaInternaModel createArea(String codigoHospital, AreaCreateBody body) throws Exception {
        String json = post("/hospitals/" + codigoHospital + "/areas", gson.toJson(body));
        return gson.fromJson(json, AreaInternaModel.class);
    }

    /**
     * Actualiza los datos de un área interna existente en el hospital.
     *
     * @param codigoHospital Código del hospital al que pertenece el área.
     * @param codigoArea     Código del área a actualizar.
     * @param body           Nuevos datos del área.
     * @return Área actualizada.
     * @throws Exception si el servidor responde con un código de error.
     */
    public AreaInternaModel updateArea(String codigoHospital, String codigoArea,
                                       AreaUpdateBody body) throws Exception {
        String json = put("/hospitals/" + codigoHospital + "/areas/" + codigoArea,
                gson.toJson(body));
        return gson.fromJson(json, AreaInternaModel.class);
    }

    /**
     * Cuerpo de la petición para crear un hospital.
     * Refleja HospitalRequest del backend.
     */
    public record HospitalCreateBody(String codigo, String nombre, String direccion,
                                     String telefono, String codigoCiudad, Boolean estado) {}

    /**
     * Cuerpo de la petición para actualizar un hospital.
     * Refleja HospitalUpdateRequest del backend.
     */
    public record HospitalUpdateBody(String nombre, String direccion,
                                     String telefono, String codigoCiudad,
                                     Boolean estado) {}

    /**
     * Cuerpo de la petición para actualizar un área interna.
     * Refleja AreaInternaUpdateRequest del backend.
     */
    public record AreaUpdateBody(String nombre, String descripcion,
                                 String codigoAreaInterna) {}

    /**
     * Cuerpo de la petición para crear un área interna.
     * Refleja AreaInternaRequest del backend.
     */
    public record AreaCreateBody(String codigo, String nombre,
                                 String descripcion, String codigoAreaInterna) {}

    /**
     * Ejecuta una petición GET al path indicado y devuelve el cuerpo de la respuesta.
     *
     * @param path Ruta relativa del endpoint (ej. "/hospitals").
     * @return Cuerpo de la respuesta como String JSON.
     * @throws Exception si el servidor responde con un código de error.
     */
    private String get(String path) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .GET()
                .build();
        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
        validarRespuesta(response);
        return response.body();
    }

    /**
     * Ejecuta una petición POST con cuerpo JSON al path indicado.
     *
     * @param path     Ruta relativa del endpoint.
     * @param jsonBody Cuerpo de la petición en formato JSON.
     * @return Cuerpo de la respuesta como String JSON.
     * @throws Exception si el servidor responde con un código de error.
     */
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

    /**
     * Ejecuta una petición PUT con cuerpo JSON al path indicado.
     *
     * @param path     Ruta relativa del endpoint.
     * @param jsonBody Cuerpo de la petición en formato JSON.
     * @return Cuerpo de la respuesta como String JSON.
     * @throws Exception si el servidor responde con un código de error.
     */
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

    /**
     * Lanza una excepción si el servidor devuelve un código de error (4xx o 5xx).
     *
     * @param response Respuesta HTTP recibida del servidor.
     * @throws Exception con el código y cuerpo del error si el status es 400 o mayor.
     */
    private void validarRespuesta(HttpResponse<String> response) throws Exception {
        if (response.statusCode() >= 400) {
            throw new Exception("Error del servidor (" + response.statusCode() + "): " + response.body());
        }
    }
}