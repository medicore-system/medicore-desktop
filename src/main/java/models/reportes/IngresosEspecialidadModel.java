package models.reportes;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public class IngresosEspecialidadModel {
    private String nombreEspecialidad;
    private BigDecimal totalIngresos;

    public String getNombreEspecialidad() { return nombreEspecialidad; }
    public void setNombreEspecialidad(String nombreEspecialidad) { this.nombreEspecialidad = nombreEspecialidad; }
    public BigDecimal getTotalIngresos() { return totalIngresos; }
    public void setTotalIngresos(BigDecimal totalIngresos) { this.totalIngresos = totalIngresos; }
}