package models;

/**
 * Representa un hospital del sistema.
 *
 * Mapea exactamente la respuesta JSON de {@code GET /hospitals} y
 * {@code GET /hospitals/{codigo}}. Los nombres de campo deben coincidir
 * con {@code HospitalResponse} del backend para que Gson los deserialice.
 */
public class HospitalModel {

    /** Identificador único del hospital (ej. {@code HOS001}). */
    public String codigo;

    /** Nombre o razón social del hospital. */
    public String nombre;

    /** Dirección física del hospital. */
    public String direccion;

    /** Número de contacto del hospital. */
    public String telefono;

    /** {@code true} si el hospital está activo; {@code false} si está inactivo. */
    public Boolean estado;

    /** Código de la ciudad donde está ubicado (FK a la tabla ciudad). */
    public String codigoCiudad;

    /** Nombre legible de la ciudad, incluido en la respuesta del servidor. */
    public String nombreCiudad;
}
