package models;

/**
 * Representa un área interna asociada a un hospital.
 *
 * Mapea exactamente la respuesta JSON de {@code GET /hospitals/{id}/areas}.
 * Los nombres de campo deben coincidir con {@code AreaInternaResponse} del
 * backend para que Gson los deserialice correctamente.
 */
public class AreaInternaModel {

    /** Identificador único del área dentro del hospital (ej. {@code HAI001}). */
    public String codigo;

    /** Nombre descriptivo del área en este hospital (ej. "Consultorio 1-A"). */
    public String nombre;

    /** Descripción adicional del área; puede ser {@code null}. */
    public String descripcion;

    /** Código del tipo genérico de área (FK a la tabla area_interna, ej. {@code AI001}). */
    public String codigoAreaInterna;

    /** Nombre del tipo genérico de área (ej. "urgencias", "pediatria"). */
    public String nombreAreaInterna;
}
