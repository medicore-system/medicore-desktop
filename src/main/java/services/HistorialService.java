package services;

import com.fasterxml.jackson.core.type.TypeReference;
import models.HistorialModel;
import services.http.HttpServiceImpl;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Servicio HTTP para gestionar los historiales clínicos.
 *
 * <p>Se conecta al backend en {@code localhost:8080/historial}.
 * Implementa el patrón Singleton.</p>
 */
public class HistorialService extends HttpServiceImpl<Object, String> {

    private static final String BASE_URL = "http://localhost:8080/historial";

    private static HistorialService instance;

    private HistorialService() {
        super(BASE_URL);
    }

    public static HistorialService getInstance() {
        if (instance == null) instance = new HistorialService();
        return instance;
    }

    /**
     * Obtiene todos los historiales clínicos registrados.
     *
     * @return lista de historiales.
     */
    public CompletableFuture<List<HistorialModel>> getAllHistoriales() {
        return getAll().thenApply(json -> {
            try {
                return mapper.readValue(json, new TypeReference<List<HistorialModel>>() {});
            } catch (Exception e) {
                throw new RuntimeException("Error parseando historiales: " + e.getMessage());
            }
        });
    }

    /**
     * Crea un nuevo historial clínico en el backend.
     *
     * @param body datos del historial.
     * @return historial creado.
     */
    public CompletableFuture<HistorialModel> crear(HistorialCreateBody body) {
        return post(body).thenApply(json -> {
            try {
                return mapper.readValue(json, HistorialModel.class);
            } catch (Exception e) {
                throw new RuntimeException("Error parseando respuesta: " + e.getMessage(), e);
            }
        });
    }

    /**
     * Cuerpo de la petición POST /historial.
     * Mapea exactamente HistorialRequest del backend.
     */
    public record HistorialCreateBody(
            String codigo,
            LocalDate fecha,
            String tipo,
            String descripcion,
            String documentoPaciente,
            String documentoMedico
    ) {}
}
