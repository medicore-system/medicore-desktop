package models.reportes;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public class IngresosEspecialidadModel {
    private String nombreEspecialidad;
    private BigDecimal ingresosTotales;

    public String getNombreEspecialidad() { return nombreEspecialidad; }
    public void setNombreEspecialidad(String nombreEspecialidad) { this.nombreEspecialidad = nombreEspecialidad; }
    public BigDecimal getIngresosTotales() { return ingresosTotales; }
    public void setingresosTotales(BigDecimal ingresosTotales) { this.ingresosTotales = ingresosTotales; }
}