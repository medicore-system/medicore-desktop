package models.reportes;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AtencionesEpsModel {
    private String nombreEps;
    private Long totalAtenciones;

    public String getNombreEps() { return nombreEps; }
    public void setNombreEps(String nombreEps) { this.nombreEps = nombreEps; }
    public Long getTotalAtenciones() { return totalAtenciones; }
    public void setTotalAtenciones(Long totalAtenciones) { this.totalAtenciones = totalAtenciones; }
}