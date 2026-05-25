package services;

import com.fasterxml.jackson.core.type.TypeReference;
import models.FacturaCajaModel;
import services.http.HttpServiceImpl;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class CajaService extends HttpServiceImpl<Object, String> {

    private static final String BASE_URL = dotenv.get("URL_API_CAJA");
    private static CajaService instance;

    private CajaService() {
        super(BASE_URL);
    }

    public static CajaService getInstance() {
        if (instance == null) instance = new CajaService();
        return instance;
    }

    public CompletableFuture<List<FacturaCajaModel>> buscarPendientes(String documento) {
        return getAllCustom("/pendientes/" + documento).thenApply(json -> {
            if (json == null || json.trim().isEmpty()) return new java.util.ArrayList<>();
            try {
                return mapper.readValue(json, new TypeReference<List<FacturaCajaModel>>() {});
            } catch (Exception e) {
                throw new RuntimeException("Error parseando facturas de caja: " + e.getMessage());
            }
        });
    }

    public CompletableFuture<byte[]> procesarPagoYDescargar(String codigoFactura) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/pagar/" + codigoFactura))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.noBody()) // Es un POST vacío
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofByteArray())
                .thenApply(resp -> {
                    if (resp.statusCode() >= 400) {
                        throw new RuntimeException("Error procesando el pago. Código: " + resp.statusCode());
                    }
                    return resp.body();
                });
    }
}