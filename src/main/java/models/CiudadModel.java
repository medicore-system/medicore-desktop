package models;
/**
 * Modelo que representa una ciudad dentro del sistema.
 * Contiene información básica como código, nombre,
 * departamento y cantidad de hospitales asociados.
 */
public class CiudadModel {
    /**
     * Código único de la ciudad.
     */
    private String codigo;
    /**
     * Nombre de la ciudad.
     */
    private String nombre;
    /**
     * Departamento al cual pertenece la ciudad.
     */
    private String departamento;

    /**
     * Cantidad de hospitales en la ciudad.
     */
    private Long totalHospitales;

    /**
     * Obtiene la cantidad de hospitales registrados en la ciudad.
     *
     * @return cantidad de hospitales
     */
    public Long getTotalHospitales() {
        return totalHospitales;
    }

    /**
     * Define la cantidad de hospitales registrados en la ciudad.
     *
     * @param cantHospitales cantidad de hospitales
     */
    public void setTotalHospitales(Long cantHospitales) {
        this.totalHospitales = cantHospitales;
    }

    /**
     * Obtiene el código de la ciudad.
     *
     * @return código de la ciudad
     */
    public String getCodigo() {
        return codigo;
    }

    /**
     * Define el código de la ciudad.
     *
     * @param codigo codigo de la ciudad
     */
    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    /**
     * Obtiene el nombre de la ciudad.
     *
     * @return nombre de la ciudad
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * Define el nombre de la ciudad.
     *
     * @param nombre nombre de la ciudad
     */
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    /**
     * Obtiene el departamento de la ciudad.
     *
     * @return departamento de la ciudad
     */
    public String getDepartamento() {
        return departamento;
    }

    /**
     * Denine el departamento de la ciudad.
     *
     * @param departamento departamento de la ciudad
     */
    public void setDepartamento(String departamento) {
        this.departamento = departamento;
    }

    /**
     * Retorna el nombre de la ciudad como representación textual.
     *
     * @return nombre de la ciudad
     */
    @Override
    public String toString() {
        return nombre;
    }
}
