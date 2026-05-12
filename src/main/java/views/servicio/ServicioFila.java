package views.servicio;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import views.common.PrecioFormato;

import java.math.BigDecimal;

/**
 * Representa una fila de la tabla de servicios en la pantalla.
 *
 * Es parecida a {@link models.ServicioModel} pero pensada para JavaFX:
 * los campos están como {@link StringProperty} en vez de String normal,
 * porque eso le permite a la tabla "engancharse" a ellos y actualizar
 * la celda automáticamente cuando cambie un dato.
 *
 * Decisiones de diseño:
 *  - El precio se guarda DOS veces:
 *      * como {@link BigDecimal} (el valor real, por si hay que
 *        hacer cuentas o enviarlo al backend),
 *      * como texto formateado ("$80.000") para mostrarlo en la
 *        tabla sin tener que formatearlo cada vez que se redibuja.
 *  - {@code campoCoincidente} no es Property porque no se muestra
 *    directamente: lo lee la celda del nombre para decir
 *    "Coincide en: <campo>" debajo del nombre.
 */
public class ServicioFila {

    private final StringProperty codigo;
    private final StringProperty nombre;
    private final StringProperty descripcion;
    private final StringProperty tipo;
    private final StringProperty precioFormateado;
    private final StringProperty estado;
    private final BigDecimal precio;

    private String campoCoincidente = "";

    /**
     * Construye una fila con los datos del servicio listos para
     * mostrar. El precio formateado se calcula automáticamente
     * a partir del precio crudo.
     *
     * @param codigo       identificador del servicio (ej: "MED-001").
     * @param nombre       nombre del servicio.
     * @param descripcion  descripción del servicio.
     * @param tipo         tipo (Consulta, Examen o Procedimiento).
     * @param precio       precio en COP. Si llega null se guarda como cero.
     * @param estado       texto "Activo" o "Inactivo".
     */
    public ServicioFila(String codigo, String nombre, String descripcion,
                        String tipo, BigDecimal precio, String estado) {
        this.codigo           = new SimpleStringProperty(codigo);
        this.nombre           = new SimpleStringProperty(nombre);
        this.descripcion      = new SimpleStringProperty(descripcion);
        this.tipo             = new SimpleStringProperty(tipo);
        this.precio           = precio == null ? BigDecimal.ZERO : precio;
        this.precioFormateado = new SimpleStringProperty(PrecioFormato.formatear(this.precio));
        this.estado           = new SimpleStringProperty(estado);
    }

    /** @return el identificador del servicio (ej: "MED-001"). */
    public String getCodigo()           { return codigo.get(); }

    /** @return el nombre del servicio. */
    public String getNombre()           { return nombre.get(); }

    /** @return la descripción del servicio. */
    public String getDescripcion()      { return descripcion.get(); }

    /** @return el tipo del servicio (Consulta, Examen o Procedimiento). */
    public String getTipo()             { return tipo.get(); }

    /** @return el precio "crudo" como BigDecimal, útil para enviarlo al backend o hacer cuentas. */
    public BigDecimal getPrecio()       { return precio; }

    /** @return el precio formateado para mostrar en pantalla (ej: "$80.000"). */
    public String getPrecioFormateado() { return precioFormateado.get(); }

    /** @return el estado del servicio como texto ("Activo" o "Inactivo"). */
    public String getEstado()           { return estado.get(); }

    /** @return la propiedad observable del código (la tabla la usa para enlazar la celda). */
    public StringProperty codigoProperty()           { return codigo; }

    /** @return la propiedad observable del nombre. */
    public StringProperty nombreProperty()           { return nombre; }

    /** @return la propiedad observable de la descripción. */
    public StringProperty descripcionProperty()      { return descripcion; }

    /** @return la propiedad observable del tipo. */
    public StringProperty tipoProperty()             { return tipo; }

    /** @return la propiedad observable del precio formateado. */
    public StringProperty precioFormateadoProperty() { return precioFormateado; }

    /** @return la propiedad observable del estado. */
    public StringProperty estadoProperty()           { return estado; }

    /**
     * Devuelve el nombre del campo donde se encontró la coincidencia
     * de búsqueda. Si está vacío significa que no se está buscando
     * nada o que esta fila no coincide.
     *
     * @return "nombre", "identificador", "tipo", "descripción" o cadena vacía.
     */
    public String getCampoCoincidente() { return campoCoincidente; }

    /**
     * Guarda en la fila el nombre del campo donde se encontró la
     * coincidencia, para que la celda del nombre lo muestre debajo.
     *
     * @param c nombre del campo coincidente. Si es null se guarda como cadena vacía.
     */
    public void setCampoCoincidente(String c) { this.campoCoincidente = c == null ? "" : c; }
}
