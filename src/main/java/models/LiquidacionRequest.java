package models;

public class LiquidacionRequest {
    private String codigoEps;
    private String fechaInicio;
    private String fechaFin;

    public LiquidacionRequest(String codigoEps, String fechaInicio, String fechaFin) {
        this.codigoEps = codigoEps;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
    }

    public String getCodigoEps() { return codigoEps; }
    public String getFechaInicio() { return fechaInicio; }
    public String getFechaFin() { return fechaFin; }
}