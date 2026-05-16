package models;

/**
 * Modelo que representa la información de un médico dentro del sistema.
 * Contiene los datos básicos y de contacto del médico, así como
 * la especialidad y ciudad asociada.
 *
 * @author kAmii
 */
public class MedicoModel {

    /**
     * Documento de identificación del médico.
     */
    private String documento;

    /**
     * Nombre del médico.
     */
    private String nombre;

    /**
     * Apellido del médico.
     */
    private String apellido;

    /**
     * Identificador de la especialidad médica.
     */
    private String NombreEspecialidad;

    /**
     * Número de teléfono del médico.
     */
    private String telefono;

    /**
     * Correo electrónico del médico.
     */
    private String email;

    /**
     * Estado actual del médico en el sistema.
     */
    private String status;

    /**
     * Código de la ciudad donde se encuentra el médico.
     */
    private String nombreCiudad;

    /**
     * Obtiene el documento del médico.
     *
     * @return documento del médico
     */
    public String getDocumento() {
        return documento;
    }

    /**
     * Establece el documento del médico.
     *
     * @param documento documento del médico
     */
    public void setDocumento(String documento) {
        this.documento = documento;
    }

    /**
     * Obtiene el nombre del médico.
     *
     * @return nombre del médico
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * Establece el nombre del médico.
     *
     * @param nombre nombre del médico
     */
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    /**
     * Obtiene el apellido del médico.
     *
     * @return apellido del médico
     */
    public String getApellido() {
        return apellido;
    }

    /**
     * Establece el apellido del médico.
     *
     * @param apellido apellido del médico
     */
    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    /**
     * Obtiene el identificador de la especialidad médica.
     *
     * @return id de la especialidad
     */
    public String  getNombreEspecialidad() {
        return NombreEspecialidad;
    }

    /**
     * Establece el identificador de la especialidad médica.
     *
     * @param nombreEspecialidad id de la especialidad
     */
    public void setNombreEspecialidad(String  nombreEspecialidad) {
        this.NombreEspecialidad = nombreEspecialidad;
    }

    /**
     * Obtiene el teléfono del médico.
     *
     * @return teléfono del médico
     */
    public String getTelefono() {
        return telefono;
    }

    /**
     * Establece el teléfono del médico.
     *
     * @param telefono teléfono del médico
     */
    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    /**
     * Obtiene el correo electrónico del médico.
     *
     * @return correo electrónico del médico
     */
    public String getEmail() {
        return email;
    }

    /**
     * Establece el correo electrónico del médico.
     *
     * @param email correo electrónico del médico
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Obtiene el estado actual del médico.
     *
     * @return estado del médico
     */
    public String getStatus() {
        return status;
    }

    /**
     * Establece el estado actual del médico.
     *
     * @param status estado del médico
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Obtiene el código de la ciudad del médico.
     *
     * @return código de la ciudad
     */
    public String getNombreCiudad() {
        return nombreCiudad;
    }

    /**
     * Establece el código de la ciudad del médico.
     *
     * @param nombreCiudad código de la ciudad
     */
    public void setNombreCiudad(String nombreCiudad) {
        this.nombreCiudad = nombreCiudad;
    }
}