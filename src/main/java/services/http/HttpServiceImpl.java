package services.http;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
/**
 * Implementación base de servicios HTTP genéricos.
 *
 * <p>
 * Proporciona operaciones CRUD básicas utilizando
 * {@link HttpClient} y serialización/deserialización JSON
 * mediante {@link ObjectMapper}.
 * </p>
 *
 * @param <T> tipo de entidad manejada por el servicio
 * @param <ID> tipo del identificador de la entidad
 */
public abstract class HttpServiceImpl <T, ID> implements IHttpService<T, ID>{
    /**
     * Cliente HTTP utilizado para realizar las peticiones.
     */
    protected final HttpClient client;
    /**
     * Mapper utilizado para convertir objetos Java a JSON y viceversa.
     */
    protected final ObjectMapper mapper;

    /**
     * URL base del recurso consumido.
     */
    protected final String baseUrl;

    /**
     * Constructor base del servicio HTTP.
     *
     * @param bu URL base del httpService
     */
    protected HttpServiceImpl(String bu){
        this.baseUrl = bu;
        this.client = HttpClient.newHttpClient();
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        this.mapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    /**
     * Realiza una petición GET para obtener todos los recursos.
     *
     * @return un {@link CompletableFuture} con la respuesta en formato JSON
     */
    @Override
    public CompletableFuture<String> getAll(){
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl))
                .header("Content-Type", "application/json")
                .GET()
                .build();
        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body);
    }

    /**
     * Realiza una petición GET para obtener un recurso por su identificador.
     *
     * @param id identificador del recurso
     * @return un {@link CompletableFuture} con la respuesta en formato JSON
     */
    @Override
    public CompletableFuture<String> getById(ID id) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/" + id))
                .header("Content-Type", "application/json")
                .GET()
                .build();
        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body);
    }

    /**
     * Realiza una petición POST para crear un nuevo recurso.
     *
     * @param body objeto a enviar en el cuerpo de la petición
     * @return un {@link CompletableFuture} con la respuesta del servidor
     */
    @Override
    public CompletableFuture<String> post(T body) {
        try{
            String json = mapper.writeValueAsString(body);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::body);
        }catch(Exception e){
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Realiza una petición PUT para actualizar un recurso existente.
     *
     * @param id identificador del recurso a actualizar
     * @param body objeto con la información actualizada
     * @return un {@link CompletableFuture} con la respuesta del servidor
     */
    @Override
    public CompletableFuture<String> put(ID id, T body) {
        try{
            String json = mapper.writeValueAsString(body);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/" + id))
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::body);
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Realiza una petición PUT customeadoa tipo: Cliente/Inhabilitar/{doc}.
     *
     * @param path identificador de la ruta especifica
     * @param body objeto con la información actualizada
     * @return un {@link CompletableFuture} con la respuesta del servidor
     */
    protected CompletableFuture<String> putCustom(String path, T body) {
        try{
            String json = body != null ? mapper.writeValueAsString(body) : "";
            System.out.println(path);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(path))
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::body);
        }catch (Exception e){
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Valida la respuesta HTTP del servidor.
     *
     * <p>
     * Si el código de estado es mayor o igual a 400,
     * se lanza una excepción con el detalle del error.
     * </p>
     *
     * @param response respuesta HTTP obtenida
     * @throws Exception si la respuesta contiene un error HTTP
     */
    @Override
    public void validateResponse(HttpResponse<String> response) throws Exception{
        if (response.statusCode() >= 400) {
            throw new Exception("Error del servidor (" + response.statusCode() + "): " + response.body());
        }
    }
}
