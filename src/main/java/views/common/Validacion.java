package views.common;

import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextInputControl;

import java.util.regex.Pattern;

/**
 * Utilidades de validación para formularios.
 *
 * <p>Cada método devuelve un mensaje de error si la validación falla,
 * o {@code null} si el valor es válido. Esto permite acumular mensajes
 * y mostrarlos al usuario de forma concisa.</p>
 */
public final class Validacion {

    /** Acepta letras, dígitos y guiones. Ej: HOS-001, AREA-12 */
    private static final Pattern CODIGO = Pattern.compile("^[A-Za-z0-9-]+$");
    /** Acepta dígitos, espacios, guiones y paréntesis. Ej: (601) 382-0000 */
    private static final Pattern TELEFONO = Pattern.compile("^[0-9()+\\-\\s]+$");

    private Validacion() {}

    public static String requerido(String etiqueta, String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            return "• " + etiqueta + " es obligatorio";
        }
        return null;
    }

    public static String longitudMax(String etiqueta, String valor, int max) {
        if (valor != null && valor.length() > max) {
            return "• " + etiqueta + " no debe superar " + max + " caracteres";
        }
        return null;
    }

    public static String formatoCodigo(String etiqueta, String valor) {
        if (valor == null || valor.isBlank()) return null;
        if (!CODIGO.matcher(valor.trim()).matches()) {
            return "• " + etiqueta + " solo permite letras, números y guiones";
        }
        return null;
    }

    public static String formatoTelefono(String valor) {
        if (valor == null || valor.isBlank()) return null;
        if (!TELEFONO.matcher(valor.trim()).matches()) {
            return "• Teléfono solo permite dígitos, espacios, paréntesis y guiones";
        }
        return null;
    }

    public static String comboSeleccionado(String etiqueta, ComboBox<?> combo) {
        if (combo.getValue() == null) {
            return "• Debes seleccionar " + etiqueta;
        }
        return null;
    }

    /** Marca el control como inválido visualmente. */
    public static void marcarInvalido(Node control, boolean invalido) {
        if (control == null) return;
        control.getStyleClass().remove("input-invalido");
        if (invalido) control.getStyleClass().add("input-invalido");
    }

    /** Limpia el estado visual de varios controles. */
    public static void limpiarEstado(Node... controles) {
        for (Node c : controles) marcarInvalido(c, false);
    }

    /** Helper común para campos de texto. */
    public static String texto(TextInputControl c) {
        return c.getText() == null ? "" : c.getText().trim();
    }
}
