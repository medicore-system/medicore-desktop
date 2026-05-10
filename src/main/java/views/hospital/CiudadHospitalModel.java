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
    public String codigo;
    public String nombre;
    public String departamento;
    public Long totalHospitales;

    @Override
    public String toString() {
        if (nombre == null) return codigo == null ? "" : codigo;
        return nombre + (codigo != null ? " (" + codigo + ")" : "");
    }
}
