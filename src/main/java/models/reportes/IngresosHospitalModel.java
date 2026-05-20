package models.reportes;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public class IngresosHospitalModel {
    private String nombreHospital;
    private BigDecimal ingresosTotales;

    public String getNombreHospital() { return nombreHospital; }
    public void setNombreHospital(String nombreHospital) { this.nombreHospital = nombreHospital; }
    public BigDecimal getIngresosTotales() { return ingresosTotales; }
    public void setIngresosTotales(BigDecimal ingresosTotales) { this.ingresosTotales = ingresosTotales; }
}