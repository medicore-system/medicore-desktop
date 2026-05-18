package services;

import com.fasterxml.jackson.core.type.TypeReference;
import models.AreaInternaModel;
import models.CiudadModel;
import models.HospitalModel;
import services.http.HttpServiceImpl;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Servicio HTTP encargado de gestionar las operaciones
 * relacionadas con hospitales y sus áreas internas.
 *
 * <p>
 * Todos los métodos son asíncronos y devuelven {@link CompletableFuture}.
 * Las vistas los consumen desde el {@code HospitalController}
 * y reaccionan a través de callbacks, evitando bloquear el hilo
 * principal de JavaFX.
 * </p>
 *
 * <h2>Endpoints cubiertos</h2>
 * <ul>
 *     <li>GET    /hospitals</li>
 *     <li>GET    /hospitals/{codigo}</li>
 *     <li>POST   /hospitals</li>
 *     <li>PUT    /hospitals/{codigo}</li>
 *     <li>GET    /hospitals/{codigo}/areas</li>
 *     <li>POST   /hospitals/{codigo}/areas</li>
 *     <li>PUT    /hospitals/{codigo}/areas/{codigoArea}</li>
 *     <li>GET    /cities</li>
 * </ul>
 *
 * <p>
 * Implementa el patrón Singleton para mantener una única instancia
 * compartida en toda la aplicación.
 * </p>
 *
 * @author Juan Sebastián López Guzmán
 * @author Cristian Camilo Salazar Arenas
 */
public class HospitalService extends HttpServiceImpl<Object, String> {

    /** URL base de la API para Hospitales. */
    private static final String BASE_URL = "http://localhost:8080/hospitals";

    /** URL base de la API para Ciudades. */
    private static final String CITIES_URL = "http://localhost:8080/cities";

    /** Instancia única del servicio. */
    private static HospitalService instance;

    /** Constructor privado para evitar instanciación externa. */
    private HospitalService() {
        super(BASE_URL);
    }

    /**
     * Obtiene la instancia única del servicio.
     *
     * @return instancia Singleton de {@code HospitalService}
     */
    public static HospitalService getInstance() {
        if (instance == null) instance = new HospitalService();
        return instance;
    }

    /**
     * Lista todos los hospitales registrados en el sistema.
     *
     * @return future con la lista de hospitales obtenida del backend
     */
    public CompletableFuture<List<HospitalModel>> getAllHospitals() {
        return getAll().thenApply(json -> parseList(json,
                new TypeReference<>() {}, "hospitales"));
    }

    /**
     * Obtiene un hospital por su código.
     *
     * @param codigo Código único del hospital
     * @return future con el hospital encontrado
     */
    public CompletableFuture<HospitalModel> getHospitalById(String codigo) {
        return getById(codigo).thenApply(json -> parse(json, HospitalModel.class, "hospital"));
    }

    /**
     * Crea un nuevo hospital en el sistema.
     *
     * @param body datos del hospital a crear
     * @return future con el hospital creado con los datos del backend
     */
    public CompletableFuture<HospitalModel> crear(HospitalCreateBody body) {
        return post(body).thenApply(json -> parse(json, HospitalModel.class, "hospital"));
    }

    /**
     * Actualiza los datos de un hospital existente.
     *
     * @param codigo Código del hospital a actualizar
     * @param body   Nuevos datos del hospital
     * @return future con el hospital actualizado
     */
    public CompletableFuture<HospitalModel> actualizar(String codigo, HospitalUpdateBody body) {
        return put(codigo, body).thenApply(json -> parse(json, HospitalModel.class, "hospital"));
    }

    /**
     * Lista todas las ciudades disponibles para asociar a un hospital.
     *
     * @return future con la lista de ciudades
     */
    public CompletableFuture<List<CiudadModel>> getAllCiudades() {
        return getCustom(CITIES_URL).thenApply(json -> parseList(json,
                new TypeReference<>() {}, "ciudades"));
    }

    /**
     * Lista las áreas internas de un hospital específico.
     *
     * @param codigoHospital Código del hospital
     * @return future con la lista de áreas del hospital
     */
    public CompletableFuture<List<AreaInternaModel>> getAreas(String codigoHospital) {
        return getCustom(BASE_URL + "/" + codigoHospital + "/areas")
                .thenApply(json -> parseList(json, new TypeReference<>() {}, "áreas"));
    }

    /**
     * Crea una nueva área interna en el hospital indicado.
     *
     * @param codigoHospital Código del hospital donde se crea el área
     * @param body           Datos del área a crear
     * @return future con el área creada
     */
    public CompletableFuture<AreaInternaModel> crearArea(String codigoHospital, AreaCreateBody body) {
        return postCustom(BASE_URL + "/" + codigoHospital + "/areas", body)
                .thenApply(json -> parse(json, AreaInternaModel.class, "área"));
    }

    /**
     * Actualiza un área interna existente del hospital indicado.
     *
     * @param codigoHospital Código del hospital al que pertenece el área
     * @param codigoArea     Código del área a actualizar
     * @param body           Nuevos datos del área
     * @return future con el área actualizada
     */
    public CompletableFuture<AreaInternaModel> actualizarArea(String codigoHospital,
                                                              String codigoArea,
                                                              AreaUpdateBody body) {
        return putCustom(BASE_URL + "/" + codigoHospital + "/areas/" + codigoArea, body)
                .thenApply(json -> parse(json, AreaInternaModel.class, "área"));
    }

    /**
     * Cuerpo de la petición para crear un hospital.
     * Refleja {@code HospitalRequest} del backend.
     */
    public record HospitalCreateBody(String codigo, String nombre, String direccion,
                                     String telefono, String codigoCiudad, Boolean estado) {}

    /**
     * Cuerpo de la petición para actualizar un hospital.
     * Refleja {@code HospitalUpdateRequest} del backend.
     */
    public record HospitalUpdateBody(String nombre, String direccion,
                                     String telefono, String codigoCiudad,
                                     Boolean estado) {}

    /**
     * Cuerpo de la petición para crear un área interna.
     * Refleja {@code AreaInternaRequest} del backend.
     */
    public record AreaCreateBody(String codigo, String nombre,
                                 String descripcion, String codigoAreaInterna) {}

    /**
     * Cuerpo de la petición para actualizar un área interna.
     * Refleja {@code AreaInternaUpdateRequest} del backend.
     */
    public record AreaUpdateBody(String nombre, String descripcion,
                                 String codigoAreaInterna) {}

    /**
     * Parsea un JSON a la clase indicada, encapsulando errores con un mensaje legible.
     *
     * @param json    Cuerpo de la respuesta del servidor
     * @param clase   Clase a la que se desea convertir
     * @param tipo    Nombre del recurso, usado en el mensaje de error
     * @param <T>     Tipo de destino
     * @return Objeto deserializado
     */
    private <T> T parse(String json, Class<T> clase, String tipo) {
        try {
            return mapper.readValue(json, clase);
        } catch (Exception e) {
            throw new RuntimeException("Error parseando " + tipo + ": " + e.getMessage(), e);
        }
    }

    /**
     * Parsea un JSON a una lista del tipo indicado, encapsulando errores.
     *
     * @param json    Cuerpo de la respuesta del servidor
     * @param tipoRef Referencia de tipo (List&lt;X&gt;) para Jackson
     * @param tipo    Nombre del recurso, usado en el mensaje de error
     * @param <T>     Tipo de destino
     * @return Lista deserializada
     */
    private <T> T parseList(String json, TypeReference<T> tipoRef, String tipo) {
        try {
            return mapper.readValue(json, tipoRef);
        } catch (Exception e) {
            throw new RuntimeException("Error parseando " + tipo + ": " + e.getMessage(), e);
        }
    }

    /**
     * Realiza un GET a una URL absoluta (fuera del recurso base de hospitales).
     *
     * @param url URL completa
     * @return future con el cuerpo JSON de la respuesta
     */
    private CompletableFuture<String> getCustom(String url) {
        java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                .uri(java.net.URI.create(url))
                .header("Content-Type", "application/json")
                .GET()
                .build();
        return client.sendAsync(request, java.net.http.HttpResponse.BodyHandlers.ofString())
                .thenApply(resp -> {
                    if (resp.statusCode() >= 400)
                        throw new RuntimeException("Error " + resp.statusCode() + ": " + resp.body());
                    return resp.body();
                });
    }

    /**
     * Realiza un POST a una URL absoluta con el cuerpo serializado a JSON.
     *
     * @param url  URL completa
     * @param body Objeto a serializar
     * @return future con el cuerpo JSON de la respuesta
     */
    private CompletableFuture<String> postCustom(String url, Object body) {
        try {
            String json = mapper.writeValueAsString(body);
            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(java.net.http.HttpRequest.BodyPublishers.ofString(json))
                    .build();
            return client.sendAsync(request, java.net.http.HttpResponse.BodyHandlers.ofString())
                    .thenApply(resp -> {
                        if (resp.statusCode() >= 400)
                            throw new RuntimeException("Error " + resp.statusCode() + ": " + resp.body());
                        return resp.body();
                    });
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }
}
