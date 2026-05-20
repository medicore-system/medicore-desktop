package controllers;

import javafx.application.Platform;
import models.EpsModel;
import models.TarifaEpsModel;
import services.EpsService;
import services.TarifaEpsService;

import java.util.List;
import java.util.function.Consumer;

public class TarifaEpsController {

    private final TarifaEpsService tarifaService = TarifaEpsService.getInstance();
    private final EpsService epsService = EpsService.getInstance();

    private Consumer<List<EpsModel>> onEpsCargadas;
    private Consumer<List<TarifaEpsModel>> onTarifasCargadas;
    private Consumer<String> onError;
    private Consumer<String> onExito;

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

    public void cargarTarifasDeEps(String codigoEps) {
        tarifaService.getTarifasPorEps(codigoEps)
            .thenAccept(tarifas -> Platform.runLater(() -> {
                if (onTarifasCargadas != null) onTarifasCargadas.accept(tarifas);
            }))
            .exceptionally(ex -> {
                if (onError != null) Platform.runLater(() -> onError.accept("Error al cargar tarifas."));
                return null;
            });
    }

    public void guardarNuevaTarifa(String codigoEps, String codigoServicio, double porcentaje) {
        java.util.Map<String, Object> body = new java.util.HashMap<>();
        body.put("codigoEps", codigoEps);
        body.put("codigoServicio", codigoServicio);
        body.put("porcentajeCobertura", porcentaje);

        tarifaService.crearTarifa(body)
            .thenAccept(res -> Platform.runLater(() -> {
                if (onExito != null) onExito.accept("Tarifa creada exitosamente.");
                cargarTarifasDeEps(codigoEps);
            }))
            .exceptionally(ex -> {
                if (onError != null) Platform.runLater(() -> onError.accept("Error al crear tarifa: " + ex.getMessage()));
                return null;
            });
    }

    public void actualizarTarifa(TarifaEpsModel tarifa) {
        java.util.Map<String, Object> body = new java.util.HashMap<>();
        body.put("codigoEps", tarifa.getCodigoEps());
        body.put("codigoServicio", tarifa.getCodigoServicio());
        body.put("porcentajeCobertura", tarifa.getPorcentajeCobertura());

        tarifaService.actualizarCobertura(tarifa.getCodigo(), body)
            .thenAccept(res -> Platform.runLater(() -> {
                if (onExito != null) onExito.accept("Porcentaje actualizado.");
                cargarTarifasDeEps(tarifa.getCodigoEps());
            }))
            .exceptionally(ex -> {
                if (onError != null) Platform.runLater(() -> onError.accept("Error al actualizar: " + ex.getMessage()));
                return null;
            });
    }

    // Setters de callbacks...
    public void setOnEpsCargadas(Consumer<List<EpsModel>> cb) { this.onEpsCargadas = cb; }
    public void setOnTarifasCargadas(Consumer<List<TarifaEpsModel>> cb) { this.onTarifasCargadas = cb; }
    public void setOnError(Consumer<String> cb) { this.onError = cb; }
    public void setOnExito(Consumer<String> cb) { this.onExito = cb; }
}