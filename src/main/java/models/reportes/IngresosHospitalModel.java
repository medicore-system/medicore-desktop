package models.reportes;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public class IngresosHospitalModel {
    private String nombreHospital;
    private BigDecimal totalIngresos;

    public String getNombreHospital() { return nombreHospital; }
    public void setNombreHospital(String nombreHospital) { this.nombreHospital = nombreHospital; }
    public BigDecimal getTotalIngresos() { return totalIngresos; }
    public void setTotalIngresos(BigDecimal totalIngresos) { this.totalIngresos = totalIngresos; }
}