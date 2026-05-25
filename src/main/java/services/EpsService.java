package services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import models.EpsModel;
import services.http.HttpServiceImpl;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * Servicio encargado de gestionar las operaciones HTTP
 * relacionadas con las EPS.
 *
 * <p>
 * Implementa el patrón Singleton para mantener una única
 * instancia compartida en toda la aplicación.
 * </p>
 */
public class EpsService extends HttpServiceImpl<Object, String> {

    /**
     * URL base de la API.
     */
    private static final String BASE_URL = dotenv.get("URL_API_EPS");

    /**
     * Instancia única del servicio.
     */
    private static EpsService instance;

    /**
     * Constructor privado para evitar instanciación externa.
     */
    private EpsService() {
        super(BASE_URL);
    }

    /**
     * Obtiene la instancia única del servicio.
     *
     * @return instancia singleton de {@code EpsService}
     */
    public static EpsService getInstance() {
        if (instance == null) instance = new EpsService();
        return instance;
    }

    /**
     * Obtiene todas las EPS registradas en el sistema.
     *
     * @return lista de EPS obtenidas desde el backend
     * @throws Exception si ocurre un error durante la petición
     *                   o el servidor responde con error HTTP
     */
    public CompletableFuture<List<EpsModel>> getAllEps() {
        return getAll().thenApply(json -> {
            try {
                return mapper.readValue(json, new TypeReference<List<EpsModel>>() {});
            }catch (Exception ex){
                throw new RuntimeException("Error parseando las Eps: " + ex.getMessage() + ex);
            }
        });
    }
}
