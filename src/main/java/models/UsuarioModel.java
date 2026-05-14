package models;
//Exactamente los mismos valores que arroja nuestro ModelResponseDTO de la Api
/**
 * Modelo que representa un usuario dentro del sistema.
 * Contiene la misma estructura de datos proporcionada
 * por el ModelResponseDTO de la API.
 */
public class UsuarioModel {
    /**
     * Documento de identificación del usuario.
     */
    private String documento;

    /**
     * Nombre del usuario.
     */
    private String nombre;

    /**
     * Apellido del usuario.
     */
    private String apellido;

    /**
     * Correo del usuario.
     */
    private String correo;

    /**
     * Eps del usuario.
     */
    private String eps;

    /**
     * Ciudad del usuario.
     */
    private String ciudad;

    /**
     * Telefono del usuario.
     */
    private String telefono;

    /**
     * Rol del usuario.
     */
    private String rol;

    /**
     * Estado del usuario.
     */
    private boolean estado;

    /**
     * Obtiene el nombre completo del usuario
     * concatenando nombre y apellido.
     *
     * @return nombre completo del usuario
     */
    public String GetNombreCompleto(){
        return nombre + " " + apellido;
    }

    /**
     * Obtiene una representación textual del estado del usuario.
     *
     * @return "Activo" si el usuario está activo,
     *         de lo contrario "Inactivo"
     */
    public String getEstadoTexto(){
        return estado ? "Activo" : "Inactivo";
    }

    /**
     * Obtiene el documento del usuario.
     *
     * @return documento del usuario
     */
    public String getDocumento() {
        return documento;
    }

    /**
     * Define el documento del usuario.
     *
     * @param documento documento de identificación
     */
    public void setDocumento(String documento) {
        this.documento = documento;
    }

    /**
     * Obtiene el nombre del usuario.
     *
     * @return nombre del usuario
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * Define el nombre del usuario.
     *
     * @param nombre nombre del usuario
     */
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    /**
     * Obtiene el apellido del usuario.
     *
     * @return apellido del usuario
     */
    public String getApellido() {
        return apellido;
    }

    /**
     * Define el apellido del usuario.
     *
     * @param apellido apellido del usuario
     */
    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    /**
     * Obtiene el correo electrónico del usuario.
     *
     * @return correo electrónico
     */
    public String getCorreo() {
        return correo;
    }

    /**
     * Define el correo electrónico del usuario.
     *
     * @param correo correo electrónico
     */
    public void setCorreo(String correo) {
        this.correo = correo;
    }

    /**
     * Obtiene la EPS del usuario.
     *
     * @return EPS asociada
     */
    public String getEps() {
        return eps;
    }

    /**
     * Define la EPS del usuario.
     *
     * @param eps EPS asociada
     */
    public void setEps(String eps) {
        this.eps = eps;
    }

    /**
     * Obtiene la ciudad del usuario.
     *
     * @return ciudad asociada
     */
    public String getCiudad() {
        return ciudad;
    }

    /**
     * Define la ciudad del usuario.
     *
     * @param ciudad ciudad asociada
     */
    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    /**
     * Obtiene el número telefónico del usuario.
     *
     * @return teléfono del usuario
     */
    public String getTelefono() {
        return telefono;
    }

    /**
     * Define el número telefónico del usuario.
     *
     * @param telefono número telefónico
     */
    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    /**
     * Obtiene el rol del usuario.
     *
     * @return rol del usuario
     */
    public String getRol() {
        return rol;
    }

    /**
     * Indica si el usuario está activo.
     *
     * @return true si está activo, false en caso contrario
     */
    public boolean isEstado() {
        return estado;
    }

    /**
     * Define el estado del usuario.
     *
     * @param estado estado del usuario
     */
    public void setEstado(boolean estado) {
        this.estado = estado;
    }
}
