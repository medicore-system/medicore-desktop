package models;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Modelo que representa una ciudad dentro del sistema.
 * Los campos usan @JsonProperty para mapear los nombres en inglés
 * que devuelve la API REST al estilo Java en español.
 */
public class CiudadModel {

    @JsonProperty("code")
    private String codigo;

    @JsonProperty("name")
    private String nombre;

    @JsonProperty("department")
    private String departamento;

    @JsonProperty("status")
    private String status;

    private Long totalHospitales;

    public String getCodigo()       { return codigo; }
    public void setCodigo(String v) { this.codigo = v; }

    public String getNombre()       { return nombre; }
    public void setNombre(String v) { this.nombre = v; }

    public String getDepartamento()       { return departamento; }
    public void setDepartamento(String v) { this.departamento = v; }

    public String getStatus()       { return status; }
    public void setStatus(String v) { this.status = v; }

    public Long getTotalHospitales()       { return totalHospitales; }
    public void setTotalHospitales(Long v) { this.totalHospitales = v; }

    public boolean isActivo() {
        return "ACTIVE".equalsIgnoreCase(status);
    }

    @Override
    public String toString() {
        return nombre;
    }
}
