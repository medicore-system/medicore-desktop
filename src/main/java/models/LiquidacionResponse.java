package models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LiquidacionResponse {
    private String codigo;
    private BigDecimal totalBruto;
    private BigDecimal totalCoberturaEps;
    private BigDecimal totalCopagoPaciente;
    private String estado;
    private List<FacturaResumenModel> facturas;

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public BigDecimal getTotalBruto() { return totalBruto; }
    public void setTotalBruto(BigDecimal totalBruto) { this.totalBruto = totalBruto; }
    public BigDecimal getTotalCoberturaEps() { return totalCoberturaEps; }
    public void setTotalCoberturaEps(BigDecimal totalCoberturaEps) { this.totalCoberturaEps = totalCoberturaEps; }
    public BigDecimal getTotalCopagoPaciente() { return totalCopagoPaciente; }
    public void setTotalCopagoPaciente(BigDecimal totalCopagoPaciente) { this.totalCopagoPaciente = totalCopagoPaciente; }
    public String getEstado() {return estado;}
    public void setEstado(String estado) {this.estado = estado;} 
    public List<FacturaResumenModel> getFacturas() { return facturas; }
    public void setFacturas(List<FacturaResumenModel> facturas) { this.facturas = facturas; }
}