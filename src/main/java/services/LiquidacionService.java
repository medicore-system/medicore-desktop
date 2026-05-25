package services;

import models.LiquidacionRequest;
import models.LiquidacionResponse;
import services.http.HttpServiceImpl;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class LiquidacionService extends HttpServiceImpl<LiquidacionRequest, String> {

  private static final String BASE_URL = dotenv.get("URL_API_LIQUIDACION");
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

  public CompletableFuture<String> conciliarPago(String codigo) {
    java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
        .uri(java.net.URI.create(BASE_URL + "/" + codigo + "/estado?nuevoEstado=PAGADA"))
        .header("Content-Type", "application/json")
        .PUT(java.net.http.HttpRequest.BodyPublishers.noBody())
        .build();
    return client.sendAsync(request, java.net.http.HttpResponse.BodyHandlers.ofString())
        .thenApply(java.net.http.HttpResponse::body);
  }

  public CompletableFuture<byte[]> descargarPdf(String codigo) {
    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(BASE_URL + "/" + codigo + "/pdf"))
        .GET()
        .build();

    return client.sendAsync(request, HttpResponse.BodyHandlers.ofByteArray())
        .thenApply(HttpResponse::body);
  }
}