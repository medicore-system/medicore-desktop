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

    public String code;
    public String name;
    public String department;
    public String status;

    @Override
    public String toString() {
        if (name == null) return code == null ? "" : code;
        return name + (code != null ? " (" + code + ")" : "");
    }
}
