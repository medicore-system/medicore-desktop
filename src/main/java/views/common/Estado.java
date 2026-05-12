package views.common;

/**
 * Aquí guardamos los textos "Activo" e "Inactivo" en constantes para
 * no escribirlos como cadenas sueltas por todo el código. Si algún
 * día hay que cambiar la palabra (por ejemplo a "Habilitado" /
 * "Deshabilitado"), basta con cambiarlas aquí.
 *
 * También tiene dos métodos para pasar entre el booleano que usa el
 * backend (true/false) y el texto que ve el usuario en pantalla.
 */
public final class Estado {

    /** Texto que se muestra cuando el servicio está habilitado. */
    public static final String ACTIVO   = "Activo";

    /** Texto que se muestra cuando el servicio está deshabilitado. */
    public static final String INACTIVO = "Inactivo";

    private Estado() {}

    /**
     * Convierte el valor booleano que llega del modelo en el texto
     * que se muestra en la tabla.
     *
     * @param estado valor que viene del backend o del JSON.
     *               Si es null se trata como inactivo.
     * @return "Activo" si {@code estado} es true, "Inactivo" en cualquier otro caso.
     */
    public static String desdeBooleano(Boolean estado) {
        return Boolean.TRUE.equals(estado) ? ACTIVO : INACTIVO;
    }

    /**
     * Hace lo contrario que {@link #desdeBooleano(Boolean)}: convierte
     * el texto seleccionado en el ComboBox del formulario en un
     * booleano para enviarlo al backend.
     *
     * @param etiqueta texto seleccionado en el ComboBox.
     * @return true solo si la etiqueta es exactamente "Activo".
     */
    public static boolean aBooleano(String etiqueta) {
        return ACTIVO.equals(etiqueta);
    }
}
