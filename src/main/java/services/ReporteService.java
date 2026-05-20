package services;

import com.fasterxml.jackson.core.type.TypeReference;
import models.reportes.*;
import services.http.HttpServiceImpl;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Servicio encargado de consumir los endpoints de Business Intelligence (BI) y Reportes.
 * Todas las llamadas son asíncronas para garantizar fluidez en los gráficos de JavaFX.
 */
public class ReporteService extends HttpServiceImpl<Object, String> {

    private static final String BASE_URL = "http://localhost:8080/api/reportes";
    private static ReporteService instance;

    private ReporteService() {
        super(BASE_URL);
    }

    public static ReporteService getInstance() {
        if (instance == null) instance = new ReporteService();
        return instance;
    }

    public CompletableFuture<List<IngresosHospitalModel>> getIngresosPorHospital() {
        return getAllCustom("/ingresos-hospital").thenApply(json -> {
            try { return mapper.readValue(json, new TypeReference<>() {}); }
            catch (Exception e) { throw new RuntimeException("Error parseando ingresos por hospital"); }
        });
    }

    public CompletableFuture<List<IngresosEspecialidadModel>> getIngresosPorEspecialidad() {
        return getAllCustom("/ingresos-especialidad").thenApply(json -> {
            try { return mapper.readValue(json, new TypeReference<>() {}); }
            catch (Exception e) { throw new RuntimeException("Error parseando ingresos por especialidad"); }
        });
    }

    public CompletableFuture<List<EstadoCarteraModel>> getEstadoCartera() {
        return getAllCustom("/estado-cartera").thenApply(json -> {
            try { return mapper.readValue(json, new TypeReference<>() {}); }
            catch (Exception e) { throw new RuntimeException("Error parseando estado de cartera"); }
        });
    }

    public CompletableFuture<List<AtencionesEpsModel>> getAtencionesPorEps() {
        return getAllCustom("/atenciones-eps").thenApply(json -> {
            try { return mapper.readValue(json, new TypeReference<>() {}); }
            catch (Exception e) { throw new RuntimeException("Error parseando atenciones por EPS"); }
        });
    }

    public CompletableFuture<List<ProductividadMedicoModel>> getProductividadMedica() {
        return getAllCustom("/productividad-medica").thenApply(json -> {
            try { return mapper.readValue(json, new TypeReference<>() {}); }
            catch (Exception e) { throw new RuntimeException("Error parseando productividad médica"); }
        });
    }
}