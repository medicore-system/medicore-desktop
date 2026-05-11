package views.servicio;

import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import views.common.Estado;

import java.util.function.Consumer;

/**
 * Arma la tabla donde se muestran los servicios en pantalla.
 *
 * Esta clase no representa la tabla en sí: lo que hace es construirla.
 * Por eso todos sus métodos son static. Dentro decide qué columnas
 * tiene la tabla, cómo se ven los botones de acciones, el badge de
 * Activo/Inactivo, etc.
 *
 * Separarla de la vista principal sirve para que {@link ServicioView}
 * no se llene con código de columnas y celdas, y sea más fácil saber
 * dónde está cada cosa.
 */
public final class TablaServicios {

    private TablaServicios() {}

    /**
     * Crea la tabla completa con todas sus columnas, lista para
     * agregarse al layout de la vista.
     *
     * Los dos parámetros son los "callbacks" que se ejecutan cuando
     * el usuario presiona uno de los botones de acción de una fila.
     * La vista pasa aquí sus propios métodos {@code abrirDialogoVer}
     * y {@code abrirDialogoEditar} y así la tabla no necesita saber
     * cómo se abren los diálogos.
     *
     * @param onVer    qué hacer cuando se presiona el ojito 👁.
     * @param onEditar qué hacer cuando se presiona el lápiz ✎.
     * @return el TableView ya configurado, listo para mostrar.
     */
    public static TableView<ServicioFila> crear(Consumer<ServicioFila> onVer,
                                                Consumer<ServicioFila> onEditar) {
        TableView<ServicioFila> tv = new TableView<>();
        tv.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tv.setPlaceholder(new Label("No hay servicios que coincidan con la búsqueda."));

        tv.getColumns().addAll(
                colCodigo(),
                colNombre(),
                colDescripcion(),
                colTipo(),
                colPrecio(),
                colEstado(),
                colAcciones(onVer, onEditar)
        );
        return tv;
    }

    /**
     * Construye la columna del identificador del servicio (ej: MED-001).
     * Es texto simple, tamaño fijo entre 100 y 110 px para que no se
     * deforme cuando se redimensiona la tabla.
     *
     * @return la columna lista para agregarse a la tabla.
     */
    private static TableColumn<ServicioFila, String> colCodigo() {
        TableColumn<ServicioFila, String> col = new TableColumn<>("Identificador");
        col.setCellValueFactory(c -> c.getValue().codigoProperty());
        col.setMaxWidth(110);
        col.setMinWidth(100);
        return col;
    }

    /**
     * Construye la columna del nombre del servicio.
     *
     * No es una columna de texto plano: cada celda contiene dos
     * etiquetas apiladas verticalmente:
     *   1. El nombre, en negrita.
     *   2. Si el usuario está buscando, una pista en cursiva que dice
     *      "Coincide en: <campo>". Si no se está buscando, queda oculta.
     *
     * Para acceder a la fila usamos {@code SimpleObjectProperty} con
     * la fila completa en lugar de {@code getIndex()}, porque el
     * índice puede quedar desactualizado cuando JavaFX recicla las
     * celdas al cambiar los datos de la tabla.
     *
     * @return la columna del nombre con su renderizado personalizado.
     */
    private static TableColumn<ServicioFila, ServicioFila> colNombre() {
        TableColumn<ServicioFila, ServicioFila> col = new TableColumn<>("Nombre del Servicio");
        col.setPrefWidth(220);
        col.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue()));
        col.setCellFactory(c -> new TableCell<>() {
            private final Label lblNombre = new Label();
            private final Label lblMatch  = new Label();
            private final VBox  contenido = new VBox(2, lblNombre, lblMatch);
            {
                lblNombre.getStyleClass().add("servicio-nombre-celda");
                lblMatch.getStyleClass().add("servicio-match-celda");
                contenido.setPadding(new Insets(4, 0, 4, 0));
            }
            @Override
            protected void updateItem(ServicioFila f, boolean empty) {
                super.updateItem(f, empty);
                if (empty || f == null) {
                    setGraphic(null);
                    return;
                }
                lblNombre.setText(f.getNombre());
                String match = f.getCampoCoincidente();
                boolean visible = match != null && !match.isEmpty();
                lblMatch.setText(visible ? "Coincide en: " + match : "");
                lblMatch.setVisible(visible);
                lblMatch.setManaged(visible);
                setGraphic(contenido);
            }
        });
        return col;
    }

    /**
     * Construye la columna de la descripción.
     *
     * Activamos {@code wrapText} para que cuando la descripción sea
     * larga se acomode en varias líneas dentro de la celda, en vez
     * de quedar cortada con puntos suspensivos.
     *
     * @return la columna de descripción con texto que se ajusta.
     */
    private static TableColumn<ServicioFila, String> colDescripcion() {
        TableColumn<ServicioFila, String> col = new TableColumn<>("Descripción");
        col.setCellValueFactory(c -> c.getValue().descripcionProperty());
        col.setCellFactory(c -> new TableCell<>() {
            private final Label lbl = new Label();
            { lbl.getStyleClass().add("servicio-desc-celda"); lbl.setWrapText(true); }
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    lbl.setText(item);
                    setGraphic(lbl);
                }
            }
        });
        return col;
    }

    /**
     * Construye la columna del tipo (Consulta, Examen o Procedimiento).
     * Es texto simple, con ancho máximo de 120 px porque los textos
     * son cortos.
     *
     * @return la columna del tipo.
     */
    private static TableColumn<ServicioFila, String> colTipo() {
        TableColumn<ServicioFila, String> col = new TableColumn<>("Tipo");
        col.setCellValueFactory(c -> c.getValue().tipoProperty());
        col.setMaxWidth(120);
        return col;
    }

    /**
     * Construye la columna del precio. Muestra el precio ya formateado
     * (ej: "$80.000"), no el número crudo.
     *
     * @return la columna del precio formateado.
     */
    private static TableColumn<ServicioFila, String> colPrecio() {
        TableColumn<ServicioFila, String> col = new TableColumn<>("Precio (COP)");
        col.setCellValueFactory(c -> c.getValue().precioFormateadoProperty());
        col.setMaxWidth(120);
        return col;
    }

    /**
     * Construye la columna del estado.
     *
     * En lugar de mostrar el texto "Activo"/"Inactivo" tal cual,
     * cada celda pinta una insignia (badge) con fondo verde para
     * Activo y fondo rojo para Inactivo. El color sale del CSS,
     * aquí solo le agregamos o quitamos las clases.
     *
     * @return la columna del estado con badges de color.
     */
    private static TableColumn<ServicioFila, String> colEstado() {
        TableColumn<ServicioFila, String> col = new TableColumn<>("Estado");
        col.setCellValueFactory(c -> c.getValue().estadoProperty());
        col.setMaxWidth(100);
        col.setCellFactory(c -> new TableCell<>() {
            private final Label badge = new Label();
            { badge.getStyleClass().add("badge-estado"); }
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                    return;
                }
                badge.setText(item);
                badge.getStyleClass().removeAll("badge-activo", "badge-inactivo");
                badge.getStyleClass().add(Estado.ACTIVO.equals(item) ? "badge-activo" : "badge-inactivo");
                setGraphic(badge);
                setText(null);
            }
        });
        return col;
    }

    /**
     * Construye la columna de acciones con los botones 👁 (Ver) y
     * ✎ (Editar).
     *
     * Cada celda guarda la fila que le toca a través del
     * {@code cellValueFactory}, y vuelve a engancharle los
     * {@code onAction} a los botones cada vez que la celda se
     * actualiza. Eso garantiza que aunque JavaFX recicle la celda,
     * el botón siempre va a actuar sobre el servicio correcto.
     *
     * @param onVer    función a ejecutar al presionar Ver.
     * @param onEditar función a ejecutar al presionar Editar.
     * @return la columna de acciones con los dos botones por fila.
     */
    private static TableColumn<ServicioFila, ServicioFila> colAcciones(Consumer<ServicioFila> onVer,
                                                                       Consumer<ServicioFila> onEditar) {
        TableColumn<ServicioFila, ServicioFila> col = new TableColumn<>("Acciones");
        col.setMaxWidth(110);
        col.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue()));
        col.setCellFactory(c -> new TableCell<>() {
            private final Button btnVer    = botonIcono("👁", "btn-icono", "btn-ver");
            private final Button btnEditar = botonIcono("✎", "btn-icono", "btn-editar");
            private final HBox contenedor  = new HBox(5, btnVer, btnEditar);
            { contenedor.setAlignment(Pos.CENTER_LEFT); }
            @Override
            protected void updateItem(ServicioFila f, boolean empty) {
                super.updateItem(f, empty);
                if (empty || f == null) {
                    setGraphic(null);
                    return;
                }
                btnVer.setOnAction(e -> onVer.accept(f));
                btnEditar.setOnAction(e -> onEditar.accept(f));
                setGraphic(contenedor);
            }
        });
        return col;
    }

    /**
     * Crea un botón pequeño con un ícono dentro y las clases CSS que
     * le pasen, para los botones de acciones de la tabla.
     *
     * @param icono  carácter o emoji que va dentro del botón (ej: "👁").
     * @param clases lista de clases CSS para darle el estilo.
     * @return el botón configurado.
     */
    private static Button botonIcono(String icono, String... clases) {
        Button btn = new Button(icono);
        btn.getStyleClass().addAll(clases);
        return btn;
    }
}
