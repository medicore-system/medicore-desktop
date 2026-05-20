package controllers;

import javafx.application.Platform;
import models.LiquidacionResponse;
import services.LiquidacionService;

import java.util.List;
import java.util.function.Consumer;

import java.io.File;
import java.nio.file.Files;

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

    private Consumer<String> onPdfExito;

    public void descargarYGuardarPdf(String codigo, File archivoDestino) {
        service.descargarPdf(codigo)
                .thenAccept(bytes -> Platform.runLater(() -> {
                    try {
                        Files.write(archivoDestino.toPath(), bytes);
                        if (onPdfExito != null)
                            onPdfExito.accept("PDF guardado correctamente en: " + archivoDestino.getName());
                    } catch (Exception e) {
                        if (onError != null)
                            onError.accept("No se pudo guardar el archivo: " + e.getMessage());
                    }
                }))
                .exceptionally(ex -> {
                    if (onError != null)
                        Platform.runLater(() -> onError.accept("Error descargando el PDF: " + ex.getMessage()));
                    return null;
                });
    }

    public void setOnPdfExito(Consumer<String> cb) {
        this.onPdfExito = cb;
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