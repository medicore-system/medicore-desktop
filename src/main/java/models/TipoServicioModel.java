package models;

/**
 * Modelo que representa un tipo de servicio médico.
 * Coincide exactamente con el TipoServicioResponse del backend.
 */
public class TipoServicioModel {

    /** Identificador numérico del tipo de servicio. */
    private Integer id;

    /** Nombre del tipo (ej: Consulta, Examen, Procedimiento). */
    private String nombre;

    /** Prefijo usado para generar el código del servicio (ej: MED, EXA). */
    private String prefijo;

    /**
     * Obtiene el id del tipo.
     *
     * @return id del tipo de servicio
     */
    public Integer getId() {
        return id;
    }

    /**
     * Define el id del tipo.
     *
     * @param id id del tipo de servicio
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * Obtiene el nombre del tipo.
     *
     * @return nombre del tipo de servicio
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * Define el nombre del tipo.
     *
     * @param nombre nombre del tipo de servicio
     */
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    /**
     * Obtiene el prefijo del tipo.
     *
     * @return prefijo del tipo de servicio
     */
    public String getPrefijo() {
        return prefijo;
    }

    /**
     * Define el prefijo del tipo.
     *
     * @param prefijo prefijo del tipo de servicio
     */
    public void setPrefijo(String prefijo) {
        this.prefijo = prefijo;
    }

    /**
     * Devuelve el nombre del tipo como texto,
     * para que el ComboBox lo muestre correctamente.
     *
     * @return nombre del tipo de servicio
     */
    @Override
    public String toString() {
        return nombre;
    }
}
