package models.reportes;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductividadMedicoModel {
    private String nombreMedico;
    private String apellidosMedico;
    private Long citasCompletadas;

    public String getNombreMedico() { return nombreMedico; }
    public void setNombreMedico(String nombreMedico) { this.nombreMedico = nombreMedico; }
    public String getApellidosMedico() { return apellidosMedico; }
    public void setApellidosMedico(String apellidosMedico) { this.apellidosMedico = apellidosMedico; }
    public Long getCitasCompletadas() { return citasCompletadas; }
    public void setCitasCompletadas(Long citasCompletadas) { this.citasCompletadas = citasCompletadas; }
    
    public String getNombreCompleto() { return nombreMedico + " " + apellidosMedico; }
}