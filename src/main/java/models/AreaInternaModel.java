package models;

/**
 * Representa la respuesta JSON que devuelve el backend en
 * GET /hospitals/{id}/areas. Los campos deben coincidir con
 * AreaInternaResponse del servidor.
 */
public class AreaInternaModel {
    public String codigo;
    public String nombre;
    public String descripcion;
    public String codigoAreaInterna;
    public String nombreAreaInterna;
}
