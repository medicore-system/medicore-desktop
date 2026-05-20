package models.reportes;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductividadMedicoModel {
    private String nombreMedico;
    private Long citasAtendidas;
    private BigDecimal ingresosGenerados;

    public String getNombreMedico() { return nombreMedico; }
    public void setNombreMedico(String nombreMedico) { this.nombreMedico = nombreMedico; }
    public Long getCitasAtendidas() { return citasAtendidas; }
    public void setCitasAtendidas(Long citasAtendidas) { this.citasAtendidas = citasAtendidas; }
    public BigDecimal getIngresosGenerados() { return ingresosGenerados; }
    public void setIngresosGenerados(BigDecimal ingresosGenerados) { this.ingresosGenerados = ingresosGenerados; }
}