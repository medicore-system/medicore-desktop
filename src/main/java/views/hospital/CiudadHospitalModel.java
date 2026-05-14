package views.hospital;

/**
 * Modelo ligero de Ciudad usado únicamente por la sección de
 * gestión de hospitales (selector de ciudad en los formularios).
 *
 * <p>Se ubica dentro del paquete {@code views.hospital} para no chocar con
 * la clase {@code CiudadModel} que el equipo de Ciudades está construyendo
 * en su propio módulo. Refleja el JSON de {@code GET /cities} del backend.</p>
 */
public class CiudadHospitalModel {

    /** Código único de la ciudad (ej. {@code BOG001}). */
    public String codigo;

    /** Nombre de la ciudad (ej. "Bogotá"). */
    public String nombre;

    /** Departamento al que pertenece la ciudad. */
    public String departamento;

    /** Cantidad de hospitales registrados en esta ciudad; puede ser {@code null}. */
    public Long totalHospitales;

    /**
     * Representación legible para el combo de ciudades en los formularios.
     *
     * @return {@code "nombre (código)"}, o solo el código si {@code nombre} es nulo.
     */
    @Override
    public String toString() {
        if (nombre == null) return codigo == null ? "" : codigo;
        return nombre + (codigo != null ? " (" + codigo + ")" : "");
    }
}
