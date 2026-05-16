package services;

import com.fasterxml.jackson.core.type.TypeReference;
import models.CiudadModel;
import services.http.HttpServiceImpl;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Servicio HTTP para ciudades.
 * Endpoint base: GET/POST /cities  |  GET/PUT/DELETE /cities/{id}
 */
public class CiudadService extends HttpServiceImpl<Object, String> {

    private static final String BASE_URL = "http://localhost:8080/cities";

    private static CiudadService instance;

    private CiudadService() { super(BASE_URL); }

    public static CiudadService getInstance() {
        if (instance == null) instance = new CiudadService();
        return instance;
    }

    /** GET /cities — devuelve la lista completa de ciudades. */
    public CompletableFuture<List<CiudadModel>> getAllCiudades() {
        return getAll().thenApply(json -> {
            System.out.println("[CiudadService] GET /cities respuesta: " + json);
            try {
                return mapper.readValue(json, new TypeReference<List<CiudadModel>>() {});
            } catch (Exception ex) {
                System.err.println("[CiudadService] Error parseando: " + ex.getMessage());
                throw new RuntimeException("Error parseando ciudades: " + ex.getMessage(), ex);
            }
        });
    }

    /**
     * POST /cities — registra una nueva ciudad.
     * El backend genera el código automáticamente.
     */
    public CompletableFuture<CiudadModel> crear(CiudadCreateBody body) {
        return post(body).thenApply(json -> {
            System.out.println("[CiudadService] POST /cities respuesta: " + json);
            try {
                return mapper.readValue(json, CiudadModel.class);
            } catch (Exception e) {
                System.err.println("[CiudadService] Error parseando crear: " + e.getMessage());
                throw new RuntimeException("Error parseando respuesta: " + e.getMessage(), e);
            }
        });
    }

    /**
     * PUT /cities/{code} — actualiza los datos de una ciudad.
     */
    public CompletableFuture<CiudadModel> actualizar(String codigo, CiudadUpdateBody body) {
        return put(codigo, body).thenApply(json -> {
            System.out.println("[CiudadService] PUT /cities/" + codigo + " respuesta: " + json);
            try {
                return mapper.readValue(json, CiudadModel.class);
            } catch (Exception e) {
                System.err.println("[CiudadService] Error parseando actualizar: " + e.getMessage());
                throw new RuntimeException("Error parseando respuesta: " + e.getMessage(), e);
            }
        });
    }

    /**
     * Cuerpo POST /cities.
     * Campos: name, department, status ("ACTIVE" | "INACTIVE").
     */
    public record CiudadCreateBody(
            String name,
            String department,
            String status
    ) {}

    /**
     * Cuerpo PUT /cities/{id}.
     * Campos: name, department, status ("ACTIVE" | "INACTIVE").
     */
    public record CiudadUpdateBody(
            String name,
            String department,
            String status
    ) {}
}
