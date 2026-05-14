package models;
/**
 * Modelo que representa una EPS dentro del sistema.
 * Contiene información básica como código y nombre.
 */
public class EpsModel {
    /**
     * Código único de la EPS.
     */
    private String codigo;

    /**
     * Nombre de la EPS.
     */
    private String nombre;

    /**
     * Obtiene el código de la EPS.
     *
     * @return código de la EPS
     */
    public String getCodigo() {
        return codigo;
    }

    /**
     * Define el código de la EPS.
     *
     * @param codigo código único de la EPS
     */
    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    /**
     * Obtiene el nombre de la EPS.
     *
     * @return nombre de la EPS
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * Define el nombre de la EPS.
     *
     * @param nombre nombre de la EPS
     */
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    /**
     * Retorna el nombre de la EPS como representación textual.
     *
     * @return nombre de la EPS
     */
    @Override
    public String toString() {
        return nombre;
    }
}
