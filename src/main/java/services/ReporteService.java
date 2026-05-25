package services;

import com.fasterxml.jackson.core.type.TypeReference;
import models.reportes.*;
import services.http.HttpServiceImpl;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Servicio encargado de consumir los endpoints de Business Intelligence (BI) y
 * Reportes.
 * Todas las llamadas son asíncronas para garantizar fluidez en los gráficos de
 * JavaFX.
 */
public class ReporteService extends HttpServiceImpl<Object, String> {

  private static final String BASE_URL = dotenv.get("URL_API_REPORTE");
  private static ReporteService instance;

  private ReporteService() {
    super(BASE_URL);
  }

  public static ReporteService getInstance() {
    if (instance == null)
      instance = new ReporteService();
    return instance;
  }

  public CompletableFuture<List<IngresosHospitalModel>> getIngresosPorHospital(int anio) {
    return getAllCustom("/ingresos-hospital?anio=" + anio).thenApply(json -> {
      if (json == null || json.trim().isEmpty())
        return new java.util.ArrayList<>();
      try {
        return mapper.readValue(json, new TypeReference<>() {
        });
      } catch (Exception e) {
        throw new RuntimeException("Error parseando ingresos hospital");
      }
    });
  }

  public CompletableFuture<List<IngresosEspecialidadModel>> getIngresosPorEspecialidad(int anio) {
    return getAllCustom("/ingresos-especialidad?anio=" + anio).thenApply(json -> {
      if (json == null || json.trim().isEmpty())
        return new java.util.ArrayList<>();
      try {
        return mapper.readValue(json, new TypeReference<>() {
        });
      } catch (Exception e) {
        throw new RuntimeException("Error parseando ingresos especialidad");
      }
    });
  }

  public CompletableFuture<List<EstadoCarteraModel>> getEstadoCartera() {
    return getAllCustom("/estado-cartera").thenApply(json -> {
      if (json == null || json.trim().isEmpty())
        return new java.util.ArrayList<>();
      try {
        return mapper.readValue(json, new TypeReference<>() {
        });
      } catch (Exception e) {
        throw new RuntimeException("Error parseando estado de cartera");
      }
    });
  }

  public CompletableFuture<List<AtencionesEpsModel>> getAtencionesPorEps(int anio) {
    return getAllCustom("/atenciones-eps?anio=" + anio).thenApply(json -> {
      if (json == null || json.trim().isEmpty())
        return new java.util.ArrayList<>();
      try {
        return mapper.readValue(json, new TypeReference<>() {
        });
      } catch (Exception e) {
        throw new RuntimeException("Error parseando atenciones por EPS");
      }
    });
  }

  public CompletableFuture<List<ProductividadMedicoModel>> getProductividadMedica(int anio, int mes) {
    return getAllCustom("/productividad-medica?anio=" + anio + "&mes=" + mes).thenApply(json -> {
      if (json == null || json.trim().isEmpty())
        return new java.util.ArrayList<>();
      try {
        return mapper.readValue(json, new TypeReference<>() {
        });
      } catch (Exception e) {
        throw new RuntimeException("Error parseando productividad médica");
      }
    });
  }
}