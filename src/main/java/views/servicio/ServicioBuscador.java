package views.servicio;

import java.util.Comparator;
import java.util.List;

/**
 * Filtra la lista de servicios según lo que el usuario haya escrito
 * en el buscador.
 *
 * Cómo funciona:
 *   1. A cada servicio se le da una "puntuación" según qué tan bien
 *      coincide con el texto buscado. Una coincidencia exacta con el
 *      nombre vale más que una coincidencia parcial en la descripción.
 *   2. Los servicios con puntuación 0 (que no coinciden en nada) se
 *      descartan.
 *   3. El resto se ordena de mayor a menor puntuación, para que las
 *      coincidencias más relevantes salgan primero.
 *
 * Además, en cada fila se guarda en qué campo se encontró la
 * coincidencia, para que la celda del nombre pueda mostrar abajo
 * algo como "Coincide en: tipo".
 */
public final class ServicioBuscador {

    private ServicioBuscador() {}

    /**
     * Filtra y ordena la lista de servicios según el texto que escribió
     * el usuario.
     *
     * Casos:
     *  - Si el texto está vacío o es null: devuelve la lista completa
     *    sin ningún cambio y limpia el "campo coincidente" de cada fila.
     *  - Si hay texto: filtra los que no coinciden, marca el campo
     *    coincidente en cada fila restante y los ordena por relevancia.
     *
     * @param servicios lista completa de servicios disponibles.
     * @param texto     lo que el usuario escribió en el buscador.
     * @return lista filtrada y ordenada (puede estar vacía si nada coincide).
     */
    public static List<ServicioFila> buscar(List<ServicioFila> servicios, String texto) {
        if (texto == null || texto.isBlank()) {
            for (ServicioFila s : servicios) s.setCampoCoincidente("");
            return servicios;
        }
        String t = texto.toLowerCase().trim();
        return servicios.stream()
                .peek(s -> s.setCampoCoincidente(detectarCampoCoincidente(s, t)))
                .filter(s -> puntuacion(s, t) > 0)
                .sorted(Comparator.comparingInt((ServicioFila s) -> puntuacion(s, t)).reversed())
                .toList();
    }

    /**
     * Calcula qué tan relevante es un servicio para el texto buscado.
     *
     * La escala (mayor número = más relevante):
     *   100 - el nombre es exactamente igual al texto
     *    95 - el código es exactamente igual al texto
     *    80 - el nombre empieza con el texto
     *    70 - el código empieza con el texto
     *    60 - el nombre contiene el texto en alguna parte
     *    50 - el tipo empieza con el texto
     *    40 - el tipo contiene el texto
     *    30 - la descripción contiene el texto
     *    15 - el código contiene el texto en alguna parte
     *     0 - no coincide en nada
     *
     * @param s     fila del servicio que se está evaluando.
     * @param texto texto de búsqueda, ya pasado a minúsculas.
     * @return puntuación de la coincidencia, o 0 si no coincide.
     */
    private static int puntuacion(ServicioFila s, String texto) {
        String nombre = s.getNombre().toLowerCase();
        String codigo = s.getCodigo().toLowerCase();
        String desc   = s.getDescripcion().toLowerCase();
        String tipo   = s.getTipo().toLowerCase();

        if (nombre.equals(texto))     return 100;
        if (codigo.equals(texto))     return 95;
        if (nombre.startsWith(texto)) return 80;
        if (codigo.startsWith(texto)) return 70;
        if (nombre.contains(texto))   return 60;
        if (tipo.startsWith(texto))   return 50;
        if (tipo.contains(texto))     return 40;
        if (desc.contains(texto))     return 30;
        if (codigo.contains(texto))   return 15;
        return 0;
    }

    /**
     * Mira en qué campo del servicio apareció la coincidencia para
     * poder mostrarlo al usuario debajo del nombre.
     *
     * Revisa en este orden de prioridad: nombre, identificador, tipo,
     * descripción.
     *
     * @param s     fila del servicio.
     * @param texto texto buscado, ya en minúsculas.
     * @return nombre del campo donde se encontró la coincidencia, o
     *         cadena vacía si no se encontró en ninguno.
     */
    private static String detectarCampoCoincidente(ServicioFila s, String texto) {
        if (s.getNombre().toLowerCase().contains(texto))      return "nombre";
        if (s.getCodigo().toLowerCase().contains(texto))      return "identificador";
        if (s.getTipo().toLowerCase().contains(texto))        return "tipo";
        if (s.getDescripcion().toLowerCase().contains(texto)) return "descripción";
        return "";
    }
}
