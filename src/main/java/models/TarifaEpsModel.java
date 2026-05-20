package models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Modelo que representa la Tarifa/Cobertura de una EPS
 * para un Servicio médico específico.
 */
public class TarifaEpsModel {
    private String codigo;
    private String codigoEps;
    private String nombreEps;
    private String codigoServicio;
    private String nombreServicio;
    private BigDecimal porcentajeCobertura;
    private Boolean estado;
    private LocalDateTime fechaCreacion;

    // Getters y Setters
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getCodigoEps() { return codigoEps; }
    public void setCodigoEps(String codigoEps) { this.codigoEps = codigoEps; }

    public String getNombreEps() { return nombreEps; }
    public void setNombreEps(String nombreEps) { this.nombreEps = nombreEps; }

    public String getCodigoServicio() { return codigoServicio; }
    public void setCodigoServicio(String codigoServicio) { this.codigoServicio = codigoServicio; }

    public String getNombreServicio() { return nombreServicio; }
    public void setNombreServicio(String nombreServicio) { this.nombreServicio = nombreServicio; }

    public BigDecimal getPorcentajeCobertura() { return porcentajeCobertura; }
    public void setPorcentajeCobertura(BigDecimal porcentajeCobertura) { this.porcentajeCobertura = porcentajeCobertura; }

    public Boolean getEstado() { return estado; }
    public void setEstado(Boolean estado) { this.estado = estado; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
}