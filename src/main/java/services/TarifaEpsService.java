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

  public CompletableFuture<String> crearTarifa(Object requestDTO) {
    return post(requestDTO);
  }

  // Obtiene las tarifas específicas de una EPS
  public CompletableFuture<List<TarifaEpsModel>> getTarifasPorEps(String codigoEps) {
    return getAllCustom("/eps/" + codigoEps).thenApply(json -> {
      try {
        return mapper.readValue(json, new TypeReference<List<TarifaEpsModel>>() {
        });
      } catch (Exception e) {
        throw new RuntimeException("Error parseando tarifas: " + e.getMessage());
      }
    });
  }

  // Llama al PUT
    public CompletableFuture<String> actualizarCobertura(String codigo, Object requestDTO) {
        return put(codigo, requestDTO);
    }
}