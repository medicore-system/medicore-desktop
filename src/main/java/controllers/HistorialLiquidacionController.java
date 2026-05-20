package controllers;

import javafx.application.Platform;
import models.LiquidacionResponse;
import services.LiquidacionService;

import java.util.List;
import java.util.function.Consumer;

public class HistorialLiquidacionController {
    
    private final LiquidacionService service = LiquidacionService.getInstance();
    
    private Consumer<List<LiquidacionResponse>> onDatosCargados;
    private Consumer<String> onError;

    public void cargarHistorial() {
        service.obtenerHistorial()
            .thenAccept(historial -> Platform.runLater(() -> {
                if (onDatosCargados != null) onDatosCargados.accept(historial);
            }))
            .exceptionally(ex -> {
                if (onError != null) Platform.runLater(() -> onError.accept(ex.getMessage()));
                return null;
            });
    }

    public void setOnDatosCargados(Consumer<List<LiquidacionResponse>> cb) { this.onDatosCargados = cb; }
    public void setOnError(Consumer<String> cb) { this.onError = cb; }
}