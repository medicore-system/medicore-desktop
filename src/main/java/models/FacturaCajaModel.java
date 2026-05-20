package models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FacturaCajaModel {
    private String codigoFactura;
    private String nombrePaciente;
    private String descripcionServicio;
    private Object fecha;
    private String nombreEps;
    private BigDecimal costoTotal;
    private BigDecimal coberturaEps;
    private BigDecimal copagoAPagar;
    private Boolean estaPagado;

    public String getCodigoFactura() { return codigoFactura; }
    public void setCodigoFactura(String codigoFactura) { this.codigoFactura = codigoFactura; }
    public String getNombrePaciente() { return nombrePaciente; }
    public void setNombrePaciente(String nombrePaciente) { this.nombrePaciente = nombrePaciente; }
    public String getDescripcionServicio() { return descripcionServicio; }
    public void setDescripcionServicio(String descripcionServicio) { this.descripcionServicio = descripcionServicio; }
    public Object getFecha() { return fecha; }
    public void setFecha(Object fecha) { this.fecha = fecha; }
    public String getNombreEps() { return nombreEps; }
    public void setNombreEps(String nombreEps) { this.nombreEps = nombreEps; }
    public BigDecimal getCostoTotal() { return costoTotal; }
    public void setCostoTotal(BigDecimal costoTotal) { this.costoTotal = costoTotal; }
    public BigDecimal getCoberturaEps() { return coberturaEps; }
    public void setCoberturaEps(BigDecimal coberturaEps) { this.coberturaEps = coberturaEps; }
    public BigDecimal getCopagoAPagar() { return copagoAPagar; }
    public void setCopagoAPagar(BigDecimal copagoAPagar) { this.copagoAPagar = copagoAPagar; }
    public Boolean getEstaPagado() { return estaPagado; }
    public void setEstaPagado(Boolean estaPagado) { this.estaPagado = estaPagado; }

    public String getFechaFormateada() {
        if (fecha instanceof java.util.List) {
            java.util.List<?> lista = (java.util.List<?>) fecha;
            if (lista.size() >= 3) {
                return String.format("%s-%02d-%02d", lista.get(0), lista.get(1), lista.get(2));
            }
        }
        return fecha != null ? fecha.toString() : "";
    }
}