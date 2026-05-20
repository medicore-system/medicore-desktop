package models.reportes;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EstadoCarteraModel {
    private String codigoEps;
    private String nombreEps;
    private Long cantidadLiquidaciones;
    private BigDecimal deudaTotal;

    public String getCodigoEps() { return codigoEps; }
    public void setCodigoEps(String codigoEps) { this.codigoEps = codigoEps; }
    public String getNombreEps() { return nombreEps; }
    public void setNombreEps(String nombreEps) { this.nombreEps = nombreEps; }
    public Long getCantidadLiquidaciones() { return cantidadLiquidaciones; }
    public void setCantidadLiquidaciones(Long cantidadLiquidaciones) { this.cantidadLiquidaciones = cantidadLiquidaciones; }
    public BigDecimal getDeudaTotal() { return deudaTotal; }
    public void setDeudaTotal(BigDecimal deudaTotal) { this.deudaTotal = deudaTotal; }
}