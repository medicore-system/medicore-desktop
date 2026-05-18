package controllers;

import javafx.application.Platform;
import models.AsignacionMedicoModel;
import services.AsignacionMedicoService;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class AsignacionMedicoController {

    private final AsignacionMedicoService asignacionService = AsignacionMedicoService.getInstance();

    private List<AsignacionMedicoModel> listaAsignaciones = new ArrayList<>();

    private Consumer<List<AsignacionMedicoModel>> onDatosActualizados;
    private Consumer<String> onError;
    private Consumer<String> onExito;

    private String ultimoDocumentoMedico;

    public void setOnDatosActualizados(Consumer<List<AsignacionMedicoModel>> cb) { this.onDatosActualizados = cb; }
    public void setOnError(Consumer<String> cb) { this.onError = cb; }
    public void setOnExito(Consumer<String> cb) { this.onExito = cb; }

    public void cargarPorMedico(String documentoMedico) {
        this.ultimoDocumentoMedico = documentoMedico;
        asignacionService.getByMedico(documentoMedico)
                .thenAccept(lista -> Platform.runLater(() -> {
                    listaAsignaciones = new ArrayList<>(lista);
                    if (onDatosActualizados != null) onDatosActualizados.accept(listaAsignaciones);
                }))
                .exceptionally(e -> {
                    Platform.runLater(() -> notificarError("No se pudieron cargar las asignaciones."));
                    return null;
                });
    }

    public void crear(AsignacionMedicoService.AsignacionCreateBody body, String nombreMedico) {
        asignacionService.crear(body)
                .thenAccept(a -> Platform.runLater(() -> {
                    if (onExito != null) onExito.accept("Asignación creada para '" + nombreMedico + "'");
                    cargarPorMedico(body.documentoMedico());
                }))
                .exceptionally(e -> {
                    Platform.runLater(() -> notificarError("Error al crear la asignación.\n" + e.getMessage()));
                    return null;
                });
    }

    public void desactivar(Integer codigo) {
        asignacionService.desactivar(codigo)
                .thenAccept(exito -> Platform.runLater(() -> {
                    if (onExito != null) onExito.accept("Asignación desactivada correctamente");
                    if (ultimoDocumentoMedico != null) cargarPorMedico(ultimoDocumentoMedico);
                }))
                .exceptionally(e -> {
                    Platform.runLater(() -> notificarError("Error al desactivar la asignación."));
                    return null;
                });
    }



    private void notificarError(String mensaje) {
        if (onError != null) onError.accept(mensaje);
    }
}