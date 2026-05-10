package models;

/**
 * Representa la respuesta JSON que devuelve el backend en GET /hospitals
 * y GET /hospitals/{id}. Los campos deben coincidir exactamente con
 * HospitalResponse del servidor para que Gson los mapee correctamente.
 */
public class HospitalModel {
    public String codigo;
    public String nombre;
    public String direccion;
    public String telefono;
    public Boolean estado;
    public String codigoCiudad;
    public String nombreCiudad;
}
