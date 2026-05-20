package services;

import models.LiquidacionRequest;
import models.LiquidacionResponse;
import services.http.HttpServiceImpl;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class LiquidacionService extends HttpServiceImpl<LiquidacionRequest, String> {

  private static final String BASE_URL = "http://localhost:8080/api/liquidaciones";
  private static LiquidacionService instance;

  private LiquidacionService() {
    super(BASE_URL);
  }

  public static LiquidacionService getInstance() {
    if (instance == null)
      instance = new LiquidacionService();
    return instance;
  }

  public CompletableFuture<LiquidacionResponse> generarLiquidacion(LiquidacionRequest request) {
    return post(request).thenApply(json -> {
      try {
        return mapper.readValue(json, LiquidacionResponse.class);
      } catch (Exception e) {
        throw new RuntimeException("Error parseando respuesta de liquidación: " + e.getMessage());
      }
    });
  }

  public CompletableFuture<List<LiquidacionResponse>> obtenerHistorial() {
    return getAll().thenApply(json -> {
      try {
        return mapper.readValue(json, new com.fasterxml.jackson.core.type.TypeReference<List<LiquidacionResponse>>() {
        });
      } catch (Exception e) {
        throw new RuntimeException("Error parseando historial: " + e.getMessage());
      }
    });
  }
}