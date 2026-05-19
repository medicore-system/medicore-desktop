package views.hospital;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

/**
 * Utilidades de construcción de UI compartidas por los diálogos del módulo
 * hospital ({@link HospitalFormDialog} y {@link AreaFormDialog}).
 *
 * <p>Centraliza el ensamblaje de campos de texto, grid de formulario,
 * filas etiqueta-control, label de errores y carga de hojas de estilo.
 * Así los diálogos se concentran en su lógica de negocio (validación y
 * armado del body) y no duplican el scaffolding visual.</p>
 *
 * <p>Esta clase es deliberadamente {@code final} y de sólo métodos estáticos:
 * no representa entidad ni mantiene estado, sólo agrupa funciones de fábrica
 * de componentes JavaFX.</p>
 */
final class FormularioUtils {

    /** Ancho mínimo recomendado para los diálogos del módulo hospital. */
    static final double ANCHO_MIN_DIALOGO = 520;

    private FormularioUtils() {}

    /**
     * Crea un {@link TextField} estilizado con valor inicial y texto de ayuda.
     *
     * @param inicial Valor inicial del campo; si es {@code null} se usa cadena vacía
     * @param prompt  Texto descriptivo mostrado cuando el campo está vacío
     */
    static TextField campoTexto(String inicial, String prompt) {
        TextField tf = new TextField(inicial == null ? "" : inicial);
        tf.setPromptText(prompt);
        tf.getStyleClass().add("campo-form");
        tf.setPrefHeight(36);
        return tf;
    }

    /**
     * Crea el {@link GridPane} base del formulario con dos columnas
     * (etiqueta a la derecha, control que crece).
     *
     * @param anchoMinEtiqueta Ancho mínimo asignado a la columna de etiqueta
     */
    static GridPane formularioGrid(double anchoMinEtiqueta) {
        GridPane grid = new GridPane();
        grid.setHgap(14);
        grid.setVgap(14);
        grid.setPadding(new Insets(22, 24, 18, 24));
        ColumnConstraints c0 = new ColumnConstraints();
        c0.setMinWidth(anchoMinEtiqueta);
        c0.setHalignment(HPos.RIGHT);
        ColumnConstraints c1 = new ColumnConstraints();
        c1.setHgrow(Priority.ALWAYS);
        c1.setFillWidth(true);
        grid.getColumnConstraints().addAll(c0, c1);
        return grid;
    }

    /**
     * Añade una fila de etiqueta + control al grid del formulario.
     *
     * @param grid     Grid destino
     * @param fila     Índice de fila donde insertar
     * @param etiqueta Texto de la etiqueta descriptiva
     * @param control  Control de entrada (TextField, ComboBox, etc.)
     */
    static void agregarFila(GridPane grid, int fila, String etiqueta, Node control) {
        Label lbl = new Label(etiqueta);
        lbl.getStyleClass().add("etiqueta-form");
        grid.add(lbl, 0, fila);
        grid.add(control, 1, fila);
        if (control instanceof Region r) GridPane.setHgrow(r, Priority.ALWAYS);
    }

    /**
     * Crea el {@link Label} de errores. Empieza oculto y sin ocupar espacio
     * en el layout. Cuando se le asigna texto vía
     * {@link #mostrarErrores(Label, String)} pasa a ser visible y crece hacia
     * abajo, ocupando todo el ancho de la fila del grid en la que se inserte.
     *
     * <p>Configuración importante para que el texto largo (varias líneas)
     * no se trunque: {@code maxWidth} sin límite, {@code wrapText} activo,
     * alineación a la izquierda y {@code hgrow ALWAYS}. Combinado con un
     * diálogo de altura calculada automáticamente, garantiza que los
     * botones del panel inferior nunca queden ocultos al mostrar errores.</p>
     */
    static Label crearErrorLabel() {
        Label l = new Label();
        l.getStyleClass().add("errores-form");
        l.setVisible(false);
        l.setManaged(false);
        l.setWrapText(true);
        l.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHalignment(l, HPos.LEFT);
        GridPane.setFillWidth(l, true);
        GridPane.setHgrow(l, Priority.ALWAYS);
        return l;
    }

    /**
     * Hace visible el label de errores y muestra el mensaje indicado.
     *
     * @param lbl     Label creado con {@link #crearErrorLabel()}
     * @param mensaje Texto de error a mostrar
     */
    static void mostrarErrores(Label lbl, String mensaje) {
        lbl.setText(mensaje);
        lbl.setVisible(true);
        lbl.setManaged(true);
    }

    /**
     * Aplica una hoja de estilos al panel del diálogo.
     *
     * @param dialog    Diálogo destino
     * @param rutaCss   Ruta del recurso CSS (ej. {@code "/styles/hospital/hospital.css"})
     */
    static void aplicarEstilos(Dialog<?> dialog, String rutaCss) {
        dialog.getDialogPane().getStylesheets().add(
                FormularioUtils.class.getResource(rutaCss).toExternalForm());
    }
}
