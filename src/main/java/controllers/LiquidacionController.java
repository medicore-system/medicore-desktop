package controllers;

import javafx.application.Platform;
import models.EpsModel;
import models.LiquidacionRequest;
import models.LiquidacionResponse;
import services.EpsService;
import services.LiquidacionService;

import java.util.List;
import java.util.function.Consumer;

public class LiquidacionController {

    private final EpsService epsService = EpsService.getInstance();
    private final LiquidacionService liquidacionService = LiquidacionService.getInstance();

    private Consumer<List<EpsModel>> onEpsCargadas;
    private Consumer<LiquidacionResponse> onLiquidacionExitosa;
    private Consumer<String> onError;

    public void cargarListaEps() {
        epsService.getAllEps()
            .thenAccept(epsList -> Platform.runLater(() -> {
                if (onEpsCargadas != null) onEpsCargadas.accept(epsList);
            }))
            .exceptionally(ex -> {
                if (onError != null) Platform.runLater(() -> onError.accept("Error al cargar EPS."));
                return null;
            });
    }

    public void procesarLiquidacion(String codigoEps, String fechaInicio, String fechaFin) {
        LiquidacionRequest request = new LiquidacionRequest(codigoEps, fechaInicio, fechaFin);
        
        liquidacionService.generarLiquidacion(request)
            .thenAccept(response -> Platform.runLater(() -> {
                if (onLiquidacionExitosa != null) onLiquidacionExitosa.accept(response);
            }))
            .exceptionally(ex -> {
                // Limpiamos el mensaje de la excepción de CompletableFuture
                String errorMsg = ex.getMessage().replace("java.lang.RuntimeException: java.lang.Exception: ", "");
                if (onError != null) Platform.runLater(() -> onError.accept(errorMsg));
                return null;
            });
    }

    public void setOnEpsCargadas(Consumer<List<EpsModel>> cb) { this.onEpsCargadas = cb; }
    public void setOnLiquidacionExitosa(Consumer<LiquidacionResponse> cb) { this.onLiquidacionExitosa = cb; }
    public void setOnError(Consumer<String> cb) { this.onError = cb; }
}