package models;

/**
 * Modelo que representa una Especialidades dentro del sistema.
 * Contiene información básica como id y nombre.
 */
public class EspecialidadModel {

    /**
     * Identificador único de la especialidad.
     */
    private Integer id;

    /**
     * Nombre de la especialidad.
     */
    private String nombre;

    /**
     * Obtiene el identificador de la especialidad.
     *
     * @return id de la especialidad.
     */
    public Integer getId() {
        return id;
    }

    /**
     * Establece el identificador de la especialidad.
     *
     * @param id nuevo identificador de la especialidad.
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * Obtiene el nombre de la especialidad.
     *
     * @return nombre de la especialidad.
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * Establece el nombre de la especialidad.
     *
     * @param nombre nuevo nombre de la especialidad.
     */
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    /**
     * Retorna el nombre de la especialidad en formato texto.
     *
     * @return nombre de la especialidad.
     */
    @Override
    public String toString() {
        return nombre;
    }
}
