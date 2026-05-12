package views.servicio;

import models.ServicioModel;
import views.common.Estado;

import java.util.List;

/**
 * Convierte los datos de un servicio entre los dos formatos que se
 * usan en la app:
 *  - {@link ServicioModel}: el formato "crudo", igual al JSON o al
 *    backend.
 *  - {@link ServicioFila}: el formato "para mostrar", con las
 *    propiedades observables que la tabla de JavaFX entiende.
 *
 * Tenerlo en una clase aparte sirve para que ni el servicio ni la
 * tabla tengan que conocer al otro. El servicio entrega ServicioModel,
 * la tabla recibe ServicioFila, y este convertidor está en el medio.
 */
public final class ConvertidorServicio {

    private ConvertidorServicio() {}

    /**
     * Convierte un servicio (formato modelo) en una fila lista para
     * agregarse a la tabla.
     *
     * Detalles:
     *  - Si la descripción viene null, se reemplaza por cadena vacía
     *    para que no falle al pintarla en la celda.
     *  - El estado booleano se traduce a texto ("Activo"/"Inactivo")
     *    usando {@link Estado#desdeBooleano(Boolean)}.
     *
     * @param modelo servicio tal como llega del JSON o del backend.
     * @return una nueva fila con los datos del servicio.
     */
    public static ServicioFila aFila(ServicioModel modelo) {
        return new ServicioFila(
                modelo.codigo,
                modelo.nombre,
                modelo.descripcion == null ? "" : modelo.descripcion,
                modelo.tipo,
                modelo.precio,
                Estado.desdeBooleano(modelo.estado));
    }

    /**
     * Aplica {@link #aFila(ServicioModel)} a cada servicio de la lista
     * y devuelve la lista equivalente de filas, en el mismo orden.
     *
     * @param modelos lista de servicios del backend / JSON.
     * @return lista de filas listas para mostrar en la tabla.
     */
    public static List<ServicioFila> aFilas(List<ServicioModel> modelos) {
        return modelos.stream().map(ConvertidorServicio::aFila).toList();
    }
}
