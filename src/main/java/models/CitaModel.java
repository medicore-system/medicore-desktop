package models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Modelo que representa una Cita dentro del sistema.
 * Contiene la misma estructura de datos proporcionada
 * por el ModelResponseDTO de la API.
 */
public class CitaModel {

    /**
     * Código único de la cita.
     */
    private String codigo;

    /**
     * Estado actual de la cita.
     *
     * <p>Valores posibles:
     * <ul>
     *     <li>APROBADA</li>
     *     <li>DENEGADA</li>
     * </ul>
     * </p>
     */
    private String estado;

    /**
     * Fecha y hora programada de la cita.
     */
    private LocalDateTime fecha;

    /**
     * Hora específica asignada para la cita.
     */
    private String hora;

    /**
     * Valor monetario de la cita.
     */
    private BigDecimal costo;

    /**
     * Nombre del tipo de cita.
     */
    private String tipoCita;

    /**
     * Nombre del usuario/paciente asociado a la cita.
     */
    private String nombreUsuario;

    /**
     * Documento o identificador del médico encargado.
     */
    private String medico;

    /**
     * Nombre del hospital donde se realizará la cita.
     */
    private String Hospital;

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }


    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public String getHora() {
        return hora;
    }

    public void setHora(String hora) {
        this.hora = hora;
    }

    public BigDecimal getCosto() {
        return costo;
    }

    public void setCosto(BigDecimal costo) {
        this.costo = costo;
    }

    public String getTipoCita() {
        return tipoCita;
    }

    public void setTipoCita(String tipoCita) {
        this.tipoCita = tipoCita;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public String getMedico() {
        return medico;
    }

    public void setMedico(String medico) {
        this.medico = medico;
    }

    public String getHospital() {
        return Hospital;
    }

    public void setHospital(String hospital) {
        Hospital = hospital;
    }
}
