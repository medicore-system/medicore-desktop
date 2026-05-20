package models.reportes;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EstadoCarteraModel {
    private String nombreEps;
    private BigDecimal deudaPendiente;

    public String getNombreEps() { return nombreEps; }
    public void setNombreEps(String nombreEps) { this.nombreEps = nombreEps; }
    public BigDecimal getDeudaPendiente() { return deudaPendiente; }
    public void setDeudaPendiente(BigDecimal deudaPendiente) { this.deudaPendiente = deudaPendiente; }
}