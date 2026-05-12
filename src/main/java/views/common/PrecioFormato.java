package views.common;

import java.math.BigDecimal;

/**
 * Tiene los métodos para mostrar y leer precios en pesos colombianos.
 *
 * La idea es no repetir el código de formateo en cada lugar donde se
 * muestra un precio. Cualquier vista que necesite mostrar un valor
 * en pantalla, o leer lo que escribió el usuario en un campo de
 * texto, debería usar esta clase.
 */
public final class PrecioFormato {

    private PrecioFormato() {}

    /**
     * Toma un número y lo convierte en texto bonito con separador de
     * miles, listo para mostrar en pantalla.
     *
     * Por ejemplo:
     *   formatear(80000)  -> "$80.000"
     *   formatear(1500000) -> "$1.500.000"
     *   formatear(null)   -> "$0"
     *
     * @param valor el precio que se quiere mostrar. Si llega null se
     *              trata como cero.
     * @return el precio formateado con el símbolo $ y los puntos de mil.
     */
    public static String formatear(BigDecimal valor) {
        long entero = valor == null ? 0L : valor.longValue();
        String sinSeparador = Long.toString(Math.abs(entero));
        StringBuilder sb = new StringBuilder();
        int contador = 0;
        for (int i = sinSeparador.length() - 1; i >= 0; i--) {
            sb.insert(0, sinSeparador.charAt(i));
            contador++;
            if (contador % 3 == 0 && i > 0) sb.insert(0, '.');
        }
        return "$" + sb;
    }

    /**
     * Hace lo contrario: toma lo que escribió el usuario en un campo
     * de texto y lo convierte en BigDecimal para poder guardarlo.
     *
     * Acepta que el usuario haya escrito puntos o comas como separadores
     * de miles (ej: "80.000" o "80,000"); simplemente los ignora antes
     * de convertir.
     *
     * Importante: este método asume que el texto YA pasó por la
     * validación {@code Validacion.numeroPositivo}, así que no controla
     * si el texto es realmente un número. Si llega algo raro, lanzará
     * {@link NumberFormatException}.
     *
     * @param valor texto crudo del campo de entrada del formulario.
     * @return el precio convertido. Si el texto está vacío devuelve {@link BigDecimal#ZERO}.
     */
    public static BigDecimal parsear(String valor) {
        if (valor == null || valor.isBlank()) return BigDecimal.ZERO;
        String limpio = valor.replace(".", "").replace(",", "").trim();
        return new BigDecimal(limpio);
    }
}
