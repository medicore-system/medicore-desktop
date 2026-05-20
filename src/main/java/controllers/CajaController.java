package controllers;

import javafx.application.Platform;
import models.FacturaCajaModel;
import services.CajaService;

import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.function.Consumer;

public class CajaController {

    private final CajaService service = CajaService.getInstance();
    
    private Consumer<List<FacturaCajaModel>> onBusquedaExitosa;
    private Consumer<String> onPagoExitoso;
    private Consumer<String> onError;

    public void buscarFacturas(String documento) {
        if (documento == null || documento.trim().isEmpty()) {
            if (onError != null) onError.accept("Debe ingresar un documento válido.");
            return;
        }

        service.buscarPendientes(documento)
                .thenAccept(facturas -> Platform.runLater(() -> {
                    if (onBusquedaExitosa != null) onBusquedaExitosa.accept(facturas);
                }))
                .exceptionally(ex -> {
                    if (onError != null) Platform.runLater(() -> onError.accept("Error buscando: " + ex.getMessage()));
                    return null;
                });
    }

    public void pagarYGenerarRecibo(String codigoFactura, File archivoDestino) {
        service.procesarPagoYDescargar(codigoFactura)
                .thenAccept(bytes -> Platform.runLater(() -> {
                    try {
                        Files.write(archivoDestino.toPath(), bytes);
                        if (onPagoExitoso != null) onPagoExitoso.accept("Pago registrado. Recibo guardado en: " + archivoDestino.getName());
                    } catch (Exception e) {
                        if (onError != null) onError.accept("Error guardando el PDF: " + e.getMessage());
                    }
                }))
                .exceptionally(ex -> {
                    if (onError != null) Platform.runLater(() -> onError.accept("Error al procesar el pago: " + ex.getMessage()));
                    return null;
                });
    }

    public void setOnBusquedaExitosa(Consumer<List<FacturaCajaModel>> cb) { this.onBusquedaExitosa = cb; }
    public void setOnPagoExitoso(Consumer<String> cb) { this.onPagoExitoso = cb; }
    public void setOnError(Consumer<String> cb) { this.onError = cb; }
}