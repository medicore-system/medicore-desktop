package services;

import com.fasterxml.jackson.core.type.TypeReference;
import models.ServicioModel;
import models.TipoServicioModel;
import services.http.HttpServiceImpl;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Servicio HTTP encargado de gestionar las operaciones
 * relacionadas con los servicios médicos del sistema.
 *
 * Se conecta al backend en localhost:8080/services.
 * Implementa el patrón Singleton para mantener una única
 * instancia compartida en toda la aplicación.
 */
public class ServicioService extends HttpServiceImpl<Object, String> {

    /** URL base de la API para Servicios. */
    private static final String BASE_URL = "http://localhost:8080/services";

    /** Instancia única del servicio. */
    private static ServicioService instance;

    /** Constructor privado para evitar instanciación externa. */
    private ServicioService() {
        super(BASE_URL);
    }

    /**
     * Obtiene la instancia única del servicio.
     *
     * @return instancia singleton de ServicioService
     */
    public static ServicioService getInstance() {
        if (instance == null) instance = new ServicioService();
        return instance;
    }

    /**
     * Obtiene todos los servicios registrados en el sistema.
     *
     * @return lista de servicios obtenidos desde el backend
     */
    public CompletableFuture<List<ServicioModel>> getAllServicios() {
        return getAll().thenApply(json -> {
            try {
                return mapper.readValue(json, new TypeReference<List<ServicioModel>>() {});
            } catch (Exception e) {
                throw new RuntimeException("Error parseando servicios: " + e.getMessage());
            }
        });
    }

    /**
     * Obtiene los tipos de servicio disponibles desde el backend.
     *
     * @return lista de tipos de servicio
     */
    public CompletableFuture<List<TipoServicioModel>> getAllTipos() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/types"))
                .header("Content-Type", "application/json")
                .GET()
                .build();
        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    try {
                        return mapper.readValue(response.body(),
                                new TypeReference<List<TipoServicioModel>>() {});
                    } catch (Exception e) {
                        throw new RuntimeException("Error parseando tipos: " + e.getMessage());
                    }
                });
    }

    /**
     * Registra un nuevo servicio en el sistema.
     * El código es generado automáticamente por el backend.
     *
     * @param body Datos del servicio a crear.
     * @return Servicio creado con los datos asignados por el backend.
     */
    public CompletableFuture<ServicioModel> crear(ServicioCreateBody body) {
        return post(body).thenApply(json -> {
            try {
                return mapper.readValue(json, ServicioModel.class);
            } catch (Exception e) {
                throw new RuntimeException("Error parseando respuesta: " + e.getMessage(), e);
            }
        });
    }

    /**
     * Actualiza los datos de un servicio existente.
     *
     * @param codigo Código del servicio a actualizar.
     * @param body   Nuevos datos del servicio.
     * @return Servicio actualizado.
     */
    public CompletableFuture<ServicioModel> actualizar(String codigo, ServicioUpdateBody body) {
        return put(codigo, body).thenApply(json -> {
            try {
                return mapper.readValue(json, ServicioModel.class);
            } catch (Exception e) {
                throw new RuntimeException("Error parseando respuesta: " + e.getMessage(), e);
            }
        });
    }

    /**
     * Inactiva un servicio en el sistema usando el endpoint DELETE.
     *
     * @param codigo Código del servicio a inactivar.
     * @return CompletableFuture vacío cuando termina.
     */
    public CompletableFuture<Void> inactivar(String codigo) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + codigo))
                .header("Content-Type", "application/json")
                .DELETE()
                .build();
        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> null);
    }

    /**
     * Cuerpo de la petición POST /services.
     * No incluye el código porque el backend lo genera automáticamente.
     * Mapea exactamente el ServicioRequest del backend.
     */
    public record ServicioCreateBody(
            String nombre,
            String descripcion,
            Integer idTipoServicio,
            BigDecimal precio
    ) {}

    /**
     * Cuerpo de la petición PUT /services/{codigo}.
     * Mapea exactamente el ServicioRequest del backend.
     */
    public record ServicioUpdateBody(
            String nombre,
            String descripcion,
            Integer idTipoServicio,
            BigDecimal precio,
            Boolean estado
    ) {}
}
