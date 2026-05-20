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
                    if (onDatosCargados != null)
                        onDatosCargados.accept(historial);
                }))
                .exceptionally(ex -> {
                    if (onError != null)
                        Platform.runLater(() -> onError.accept(ex.getMessage()));
                    return null;
                });
    }

    private Runnable onPagoConciliado;

    public void marcarComoPagada(String codigo) {
        service.conciliarPago(codigo)
                .thenAccept(res -> Platform.runLater(() -> {
                    if (onPagoConciliado != null)
                        onPagoConciliado.run();
                    cargarHistorial(); // Recargamos para ver el cambio de estado en vivo
                }))
                .exceptionally(ex -> {
                    if (onError != null)
                        Platform.runLater(() -> onError.accept("Error al procesar pago: " + ex.getMessage()));
                    return null;
                });
    }

    public void setOnPagoConciliado(Runnable cb) {
        this.onPagoConciliado = cb;
    }

    public void setOnDatosCargados(Consumer<List<LiquidacionResponse>> cb) {
        this.onDatosCargados = cb;
    }

    public void setOnError(Consumer<String> cb) {
        this.onError = cb;
    }
}