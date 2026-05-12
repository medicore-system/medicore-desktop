package models;

import java.math.BigDecimal;

/**
 * Guarda los datos de un servicio médico tal como vienen del JSON
 * quemado (o del backend cuando esté listo).
 *
 * Los atributos están en public a propósito: Gson los rellena
 * automáticamente al leer el JSON porque sus nombres coinciden con
 * los del archivo. Así no necesitamos escribir getters ni setters.
 *
 * Atributos:
 *  - codigo:      identificador único del servicio (ej: "MED-001").
 *  - nombre:      nombre del servicio (ej: "Consulta Médica General").
 *  - descripcion: explicación breve de qué incluye el servicio.
 *  - tipo:        Consulta, Examen o Procedimiento.
 *  - precio:      valor en pesos colombianos.
 *  - estado:      true = Activo, false = Inactivo.
 */
public class ServicioModel {
    public String codigo;
    public String nombre;
    public String descripcion;
    public String tipo;
    public BigDecimal precio;
    public Boolean estado;
}
