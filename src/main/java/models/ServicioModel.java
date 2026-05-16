package models;

import java.math.BigDecimal;

/**
 * Modelo que representa un servicio médico dentro del sistema.
 * Contiene exactamente los mismos atributos que devuelve el
 * ServicioResponse del backend.
 */
public class ServicioModel {

    /** Código identificador del servicio, generado automáticamente por el backend. */
    private String codigo;

    /** Nombre del servicio. */
    private String nombre;

    /** Descripción del servicio. */
    private String descripcion;

    /** ID del tipo de servicio (referencia a TipoServicio en el backend). */
    private Integer idTipoServicio;

    /** Nombre del tipo de servicio (ej: Consulta, Examen, Procedimiento). */
    private String tipo;

    /** Precio del servicio en pesos colombianos. */
    private BigDecimal precio;

    /** Estado del servicio: true si está activo, false si está inactivo. */
    private Boolean estado;

    /** Descripción del procedimiento médico (opcional). */
    private String procedimiento;

    /** Resultados esperados del servicio (opcional). */
    private String resultados;

    /** Código de historial clínico asociado (opcional). */
    private String codigoHistorial;

    /**
     * Devuelve el estado del servicio como texto legible.
     *
     * @return "Activo" si está activo, "Inactivo" en caso contrario.
     */
    public String getEstadoTexto() {
        return Boolean.TRUE.equals(estado) ? "Activo" : "Inactivo";
    }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public Integer getIdTipoServicio() { return idTipoServicio; }
    public void setIdTipoServicio(Integer idTipoServicio) { this.idTipoServicio = idTipoServicio; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }

    public Boolean getEstado() { return estado; }
    public void setEstado(Boolean estado) { this.estado = estado; }

    public String getProcedimiento() { return procedimiento; }
    public void setProcedimiento(String procedimiento) { this.procedimiento = procedimiento; }

    public String getResultados() { return resultados; }
    public void setResultados(String resultados) { this.resultados = resultados; }

    public String getCodigoHistorial() { return codigoHistorial; }
    public void setCodigoHistorial(String codigoHistorial) { this.codigoHistorial = codigoHistorial; }
}
