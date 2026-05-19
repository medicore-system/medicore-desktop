package services;

import com.fasterxml.jackson.core.type.TypeReference;
import models.TarifaEpsModel;
import services.http.HttpServiceImpl;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Servicio encargado de la comunicación asíncrona con la API
 * de Tarifas y Coberturas Financieras.
 * Patrón Singleton aplicado.
 */
public class TarifaEpsService extends HttpServiceImpl<Object, String> {

    private static final String BASE_URL = "http://localhost:8080/api/tarifas-eps";
    private static TarifaEpsService instance;

    private TarifaEpsService() {
        super(BASE_URL);
    }

    public static TarifaEpsService getInstance() {
        if (instance == null) {
            instance = new TarifaEpsService();
        }
        return instance;
    }

    /**
     * Obtiene la lista asíncrona de todas las tarifas.
     */
    public CompletableFuture<List<TarifaEpsModel>> getAllTarifas() {
        return getAll().thenApply(json -> {
            try {
                // mapper es protegido y provisto por HttpServiceImpl
                return mapper.readValue(json, new TypeReference<List<TarifaEpsModel>>() {});
            } catch (Exception e) {
                throw new RuntimeException("Error parseando las tarifas: " + e.getMessage());
            }
        });
    }

    /**
     * Actualiza el porcentaje de cobertura de una tarifa.
     */
    public CompletableFuture<String> actualizarCobertura(String codigo, Object requestDTO) {
        return put(codigo, requestDTO);
    }
}