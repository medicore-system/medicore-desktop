package models;

/**
 * Modelo que representa un historial clínico dentro del sistema.
 *
 * <p>Coincide con el {@code HistorialResponse} del backend.
 * Incluye los datos básicos del historial más los nombres
 * resueltos del paciente y del médico.</p>
 */
public class HistorialModel {

    /** Código único del historial clínico, por ejemplo {@code HC001}. */
    private String codigo;

    /** Fecha de la atención médica en formato {@code YYYY-MM-DD}. */
    private String fecha;

    /** Tipo de atención: Consulta, Urgencia, Procedimiento, Examen o Control. */
    private String tipo;

    /** Descripción general de la atención médica registrada. */
    private String descripcion;

    /** Documento de identificación del paciente atendido. */
    private String documentoPaciente;

    /** Nombre completo del paciente resuelto desde el backend. */
    private String nombrePaciente;

    /** Documento de identificación del médico que realizó la atención. */
    private String documentoMedico;

    /** Nombre completo del médico resuelto desde el backend. */
    private String nombreMedico;

    /**
     * Obtiene el código único del historial clínico.
     *
     * @return código del historial, por ejemplo {@code HC001}
     */
    public String getCodigo() { return codigo; }

    /**
     * Define el código único del historial clínico.
     *
     * @param codigo código asignado al historial
     */
    public void setCodigo(String codigo) { this.codigo = codigo; }

    /**
     * Obtiene la fecha de la atención médica.
     *
     * @return fecha en formato {@code YYYY-MM-DD}
     */
    public String getFecha() { return fecha; }

    /**
     * Define la fecha de la atención médica.
     *
     * @param fecha fecha en formato {@code YYYY-MM-DD}
     */
    public void setFecha(String fecha) { this.fecha = fecha; }

    /**
     * Obtiene el tipo de atención médica registrada.
     *
     * @return tipo de atención: Consulta, Urgencia, Procedimiento, Examen o Control
     */
    public String getTipo() { return tipo; }

    /**
     * Define el tipo de atención médica registrada.
     *
     * @param tipo tipo de atención médica
     */
    public void setTipo(String tipo) { this.tipo = tipo; }

    /**
     * Obtiene la descripción general de la atención médica.
     *
     * @return descripción de la atención
     */
    public String getDescripcion() { return descripcion; }

    /**
     * Define la descripción general de la atención médica.
     *
     * @param descripcion descripción de la atención
     */
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    /**
     * Obtiene el documento de identificación del paciente.
     *
     * @return documento del paciente
     */
    public String getDocumentoPaciente() { return documentoPaciente; }

    /**
     * Define el documento de identificación del paciente.
     *
     * @param documentoPaciente documento del paciente
     */
    public void setDocumentoPaciente(String documentoPaciente) { this.documentoPaciente = documentoPaciente; }

    /**
     * Obtiene el nombre completo del paciente resuelto desde el backend.
     *
     * @return nombre completo del paciente
     */
    public String getNombrePaciente() { return nombrePaciente; }

    /**
     * Define el nombre completo del paciente.
     *
     * @param nombrePaciente nombre completo del paciente
     */
    public void setNombrePaciente(String nombrePaciente) { this.nombrePaciente = nombrePaciente; }

    /**
     * Obtiene el documento de identificación del médico tratante.
     *
     * @return documento del médico
     */
    public String getDocumentoMedico() { return documentoMedico; }

    /**
     * Define el documento de identificación del médico tratante.
     *
     * @param documentoMedico documento del médico
     */
    public void setDocumentoMedico(String documentoMedico) { this.documentoMedico = documentoMedico; }

    /**
     * Obtiene el nombre completo del médico tratante resuelto desde el backend.
     *
     * @return nombre completo del médico
     */
    public String getNombreMedico() { return nombreMedico; }

    /**
     * Define el nombre completo del médico tratante.
     *
     * @param nombreMedico nombre completo del médico
     */
    public void setNombreMedico(String nombreMedico) { this.nombreMedico = nombreMedico; }

    /**
     * Representación en texto del historial para usar en ComboBox.
     * Muestra el código y el nombre del paciente.
     *
     * @return cadena con formato {@code HC001 — juan garcia}
     */
    @Override
    public String toString() {
        return codigo + " — " + (nombrePaciente != null ? nombrePaciente : "");
    }
}
