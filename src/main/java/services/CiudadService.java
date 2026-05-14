package services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import models.CiudadModel;
import services.http.HttpServiceImpl;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Servicio encargado de gestionar las operaciones HTTP
 * relacionadas con las ciudades.
 *
 * <p>
 * Implementa el patrón Singleton para mantener una única
 * instancia compartida en toda la aplicación.
 * </p>
 */
public class CiudadService extends HttpServiceImpl<Object, String> {

    /**
     * URL base de la API.
     */
    private static final String BASE_URL = "http://localhost:8080/cities";

    /**
     * Instancia única del servicio.
     */
    private static CiudadService instance;

    /**
     * Constructor privado para evitar instanciación externa.
     */
    private CiudadService() {
        super(BASE_URL);
    }

    /**
     * Obtiene la instancia única del servicio.
     *
     * @return instancia singleton de {@code CiudadService}
     */
    public static CiudadService getInstance() {
        if (instance == null) instance = new CiudadService();
        return instance;
    }

    /**
     * Obtiene todas las ciudades registradas en el sistema.
     *
     * @return lista de ciudades obtenidas desde el backend
     * @throws Exception si ocurre un error durante la petición
     *                   o el servidor responde con error HTTP
     */
    public CompletableFuture<List<CiudadModel>> getAllCiudades() {
        return getAll().thenApply(json -> {
            try{
                return mapper.readValue(json, new TypeReference<List<CiudadModel>>() {});
            }catch (Exception ex){
                throw new RuntimeException("Error parseando ciudades" + ex.getMessage(), ex);

            }
        });
    }
}
