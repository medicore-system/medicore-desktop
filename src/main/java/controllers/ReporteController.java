package controllers;

import javafx.application.Platform;
import models.reportes.*;
import services.ReporteService;

import java.util.List;
import java.util.function.Consumer;

/**
 * Controlador para la vista de Reportes (Business Intelligence).
 * Gestiona la carga asíncrona de todos los dashboards.
 */
public class ReporteController {

    private final ReporteService service = ReporteService.getInstance();

    private Consumer<List<IngresosHospitalModel>> onIngresosHospitalCargados;
    private Consumer<List<IngresosEspecialidadModel>> onIngresosEspecialidadCargados;
    private Consumer<List<EstadoCarteraModel>> onEstadoCarteraCargado;
    private Consumer<List<AtencionesEpsModel>> onAtencionesEpsCargadas;
    private Consumer<List<ProductividadMedicoModel>> onProductividadMedicaCargada;
    private Consumer<String> onError;

    public void cargarTodosLosReportes(int anio, int mes) {
        cargarIngresosHospital(anio);
        cargarIngresosEspecialidad(anio);
        cargarEstadoCartera();
        cargarAtencionesEps(anio);
        cargarProductividadMedica(anio, mes);
    }

    private void cargarIngresosHospital(int anio) {
        service.getIngresosPorHospital(anio)
                .thenAccept(datos -> Platform.runLater(() -> {
                    if (onIngresosHospitalCargados != null)
                        onIngresosHospitalCargados.accept(datos);
                }))
                .exceptionally(this::manejarError);
    }

    private void cargarIngresosEspecialidad(int anio) {
        service.getIngresosPorEspecialidad(anio)
                .thenAccept(datos -> Platform.runLater(() -> {
                    if (onIngresosEspecialidadCargados != null)
                        onIngresosEspecialidadCargados.accept(datos);
                }))
                .exceptionally(this::manejarError);
    }

    private void cargarEstadoCartera() {
        service.getEstadoCartera()
                .thenAccept(datos -> Platform.runLater(() -> {
                    if (onEstadoCarteraCargado != null)
                        onEstadoCarteraCargado.accept(datos);
                }))
                .exceptionally(this::manejarError);
    }

    private void cargarAtencionesEps(int anio) {
        service.getAtencionesPorEps(anio)
                .thenAccept(datos -> Platform.runLater(() -> {
                    if (onAtencionesEpsCargadas != null)
                        onAtencionesEpsCargadas.accept(datos);
                }))
                .exceptionally(this::manejarError);
    }

    private void cargarProductividadMedica(int anio, int mes) {
        service.getProductividadMedica(anio, mes)
                .thenAccept(datos -> Platform.runLater(() -> {
                    if (onProductividadMedicaCargada != null)
                        onProductividadMedicaCargada.accept(datos);
                }))
                .exceptionally(this::manejarError);
    }

    private Void manejarError(Throwable ex) {
        if (onError != null) {
            String msj = ex.getMessage() != null ? ex.getMessage().replace("java.lang.RuntimeException: ", "")
                    : "Error desconocido";
            Platform.runLater(() -> onError.accept("Error cargando reporte: " + msj));
        }
        return null;
    }

    public void setOnIngresosHospitalCargados(Consumer<List<IngresosHospitalModel>> cb) {
        this.onIngresosHospitalCargados = cb;
    }

    public void setOnIngresosEspecialidadCargados(Consumer<List<IngresosEspecialidadModel>> cb) {
        this.onIngresosEspecialidadCargados = cb;
    }

    public void setOnEstadoCarteraCargado(Consumer<List<EstadoCarteraModel>> cb) {
        this.onEstadoCarteraCargado = cb;
    }

    public void setOnAtencionesEpsCargadas(Consumer<List<AtencionesEpsModel>> cb) {
        this.onAtencionesEpsCargadas = cb;
    }

    public void setOnProductividadMedicaCargada(Consumer<List<ProductividadMedicoModel>> cb) {
        this.onProductividadMedicaCargada = cb;
    }

    public void setOnError(Consumer<String> cb) {
        this.onError = cb;
    }
}