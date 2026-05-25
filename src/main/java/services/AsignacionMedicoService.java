package services;

import com.fasterxml.jackson.core.type.TypeReference;
import models.AsignacionMedicoModel;
import models.auth.SessionManager;
import services.http.HttpServiceImpl;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class AsignacionMedicoService extends HttpServiceImpl<Object, Integer> {

    private static final String BASE_URL = dotenv.get("URL_API_ASIGNACIONES","http://localhost:8080/asignaciones");
    private static AsignacionMedicoService instance;

    private AsignacionMedicoService() {
        super(BASE_URL);
    }

    public static AsignacionMedicoService getInstance() {
        if (instance == null) instance = new AsignacionMedicoService();
        return instance;
    }

    public CompletableFuture<List<AsignacionMedicoModel>> getByMedico(String documentoMedico) {
        return getById2("/medico/" + documentoMedico).thenApply(json -> {
            try {
                return mapper.readValue(json, new TypeReference<List<AsignacionMedicoModel>>() {});
            } catch (Exception e) {
                throw new RuntimeException("Error parseando asignaciones: " + e.getMessage());
            }
        });
    }

    public CompletableFuture<AsignacionMedicoModel> crear(AsignacionCreateBody body) {
        return post(body).thenApply(json -> {
            try {
                return mapper.readValue(json, AsignacionMedicoModel.class);
            } catch (Exception e) {
                throw new RuntimeException("Error parseando respuesta: " + e.getMessage());
            }
        });
    }

    private CompletableFuture<String> getById2(String path) {
        java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                .uri(java.net.URI.create(BASE_URL + path))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + SessionManager.getInstance().getToken())
                .GET()
                .build();
        return client.sendAsync(request, java.net.http.HttpResponse.BodyHandlers.ofString())
                .thenApply(resp -> {
                    if (resp.statusCode() >= 400)
                        throw new RuntimeException("Error " + resp.statusCode() + ": " + resp.body());
                    return resp.body();
                });
    }

    public CompletableFuture<Boolean> desactivar(Integer codigo) {
        return delete(codigo).thenApply(resp -> true);
    }

    public record AsignacionCreateBody(
            String documentoMedico,
            String codigoCiudad,
            Integer duracionDias,
            String codigoHospital   // opcional, null = round-robin
    ) {}
}
