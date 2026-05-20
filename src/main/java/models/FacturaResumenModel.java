package models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FacturaResumenModel {
    private String codigo;
    private Object fecha;
    private String descripcion;
    private BigDecimal costoTotal;

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public Object getFecha() { return fecha; }
    public void setFecha(Object fecha) { this.fecha = fecha; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public BigDecimal getCostoTotal() { return costoTotal; }
    public void setCostoTotal(BigDecimal costoTotal) { this.costoTotal = costoTotal; }

    public String getFechaFormateada() {
        if (fecha == null) return "";
        if (fecha instanceof java.util.List) {
            java.util.List<?> lista = (java.util.List<?>) fecha;
            if(lista.size() >= 3) {
                return String.format("%s-%02d-%02d", lista.get(0), lista.get(1), lista.get(2));
            }
        }
        return fecha.toString();
    }
}