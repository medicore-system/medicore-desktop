package views.servicio;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import services.ServicioService;
import views.common.Estado;
import views.common.PrecioFormato;
import views.common.Validacion;

import java.util.List;
import java.util.function.Predicate;

/**
 * Aquí construimos los pop-ups (diálogos) del módulo de servicios:
 * el de crear, el de editar y el de ver. Además aquí hacemos la
 * validación de los campos antes de aceptar lo que escribió el usuario.
 *
 * Lo separamos para que la vista principal no se llene de código de
 * formularios y sea más fácil de leer.
 */
public final class ServicioFormulario {

    private static final String STYLESHEET = "/styles/servicio/servicio.css";
    private static final String CLASE_DIALOGO = "dialogo-servicio";

    private ServicioFormulario() {}

    /**
     * Construye el pop-up de creación de un servicio nuevo.
     *
     * Lo que hace paso a paso:
     *  1. Crea un diálogo vacío con el estilo común del módulo.
     *  2. Crea los campos del formulario (identificador, nombre,
     *     descripción, tipo, precio y estado) y los acomoda en un grid.
     *  3. Le pone los botones "Crear servicio" y "Cancelar".
     *  4. Conecta un filtro al botón "Crear" que valida todos los
     *     campos antes de aceptar; si hay errores, muestra el mensaje
     *     dentro del propio diálogo y NO lo cierra.
     *  5. Si todo valida, arma un {@link ServicioService.ServicioCreateBody}
     *     con los datos y se lo entrega al que llamó a este método.
     *
     * @param codigoYaExiste función que recibe un código y devuelve
     *                       true si ya hay un servicio con ese código.
     *                       Se la pasa la vista, que es quien tiene la
     *                       lista actual de servicios en memoria.
     * @return el diálogo listo para llamarle {@code showAndWait()}.
     *         Devolverá los datos cuando el usuario confirme, o un
     *         Optional vacío si cancela.
     */
    public static Dialog<ServicioService.ServicioCreateBody> dialogoCrear(Predicate<String> codigoYaExiste) {
        Dialog<ServicioService.ServicioCreateBody> dialog = baseDialog(
                "Nuevo Servicio", "Registrar un nuevo servicio", 520, 540);

        TextField txtCodigo = campoTexto("", "Ej: MED-006");
        TextField txtNombre = campoTexto("", "Nombre del servicio");
        TextField txtDesc   = campoTexto("", "Descripción del servicio");
        TextField txtPrecio = campoTexto("", "Ej: 80000");
        ComboBox<String> cmbTipo   = comboTipo(null);
        ComboBox<String> cmbEstado = comboEstado(Estado.ACTIVO);
        Label lblErrores = etiquetaErrores();

        GridPane grid = grid();
        agregarFila(grid, 0, "Identificador", txtCodigo);
        agregarFila(grid, 1, "Nombre",        txtNombre);
        agregarFila(grid, 2, "Descripción",   txtDesc);
        agregarFila(grid, 3, "Tipo",          cmbTipo);
        agregarFila(grid, 4, "Precio",        txtPrecio);
        agregarFila(grid, 5, "Estado",        cmbEstado);
        grid.add(lblErrores, 0, 6, 2, 1);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        ((Button) dialog.getDialogPane().lookupButton(ButtonType.OK)).setText("Crear servicio");
        ((Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL)).setText("Cancelar");

        Button btnOk = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        btnOk.addEventFilter(javafx.event.ActionEvent.ACTION, ev -> {
            String error = validarCreacion(txtCodigo, txtNombre, txtDesc, txtPrecio,
                                           cmbTipo, cmbEstado, codigoYaExiste);
            if (error != null) {
                mostrarErroresInline(lblErrores, error);
                ev.consume();
            }
        });

        dialog.setResultConverter(btn -> {
            if (btn != ButtonType.OK) return null;
            return new ServicioService.ServicioCreateBody(
                    Validacion.texto(txtCodigo),
                    Validacion.texto(txtNombre),
                    Validacion.texto(txtDesc),
                    cmbTipo.getValue(),
                    PrecioFormato.parsear(Validacion.texto(txtPrecio)),
                    Estado.aBooleano(cmbEstado.getValue()));
        });
        return dialog;
    }

    /**
     * Construye el pop-up de edición para un servicio que ya existe.
     *
     * Funciona casi igual que {@link #dialogoCrear}, pero con dos
     * diferencias importantes:
     *  - Los campos se inicializan con los datos actuales del servicio
     *    (los que vienen en {@code fila}).
     *  - NO hay campo de identificador: el código no se puede cambiar
     *    al editar.
     *  - La validación no revisa unicidad (porque el código no cambia).
     *
     * @param fila la fila de la tabla que el usuario seleccionó. De
     *             ahí se sacan los valores iniciales del formulario.
     * @return el diálogo listo para mostrar. Al confirmar entrega un
     *         {@link ServicioService.ServicioUpdateBody}; al cancelar
     *         devuelve un Optional vacío.
     */
    public static Dialog<ServicioService.ServicioUpdateBody> dialogoEditar(ServicioFila fila) {
        Dialog<ServicioService.ServicioUpdateBody> dialog = baseDialog(
                "Editar Servicio", "Editando: " + fila.getNombre(), 520, 480);

        TextField txtNombre = campoTexto(fila.getNombre(),      "Nombre del servicio");
        TextField txtDesc   = campoTexto(fila.getDescripcion(), "Descripción del servicio");
        TextField txtPrecio = campoTexto(String.valueOf(fila.getPrecio().longValue()),
                                         "Ej: 80000");
        ComboBox<String> cmbTipo   = comboTipo(fila.getTipo());
        ComboBox<String> cmbEstado = comboEstado(fila.getEstado());
        Label lblErrores = etiquetaErrores();

        GridPane grid = grid();
        agregarFila(grid, 0, "Nombre",      txtNombre);
        agregarFila(grid, 1, "Descripción", txtDesc);
        agregarFila(grid, 2, "Tipo",        cmbTipo);
        agregarFila(grid, 3, "Precio",      txtPrecio);
        agregarFila(grid, 4, "Estado",      cmbEstado);
        grid.add(lblErrores, 0, 5, 2, 1);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        ((Button) dialog.getDialogPane().lookupButton(ButtonType.OK)).setText("Guardar cambios");
        ((Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL)).setText("Cancelar");

        Button btnOk = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        btnOk.addEventFilter(javafx.event.ActionEvent.ACTION, ev -> {
            String error = validarComun(txtNombre, txtDesc, txtPrecio, cmbTipo, cmbEstado);
            if (error != null) {
                mostrarErroresInline(lblErrores, error);
                ev.consume();
            }
        });

        dialog.setResultConverter(btn -> {
            if (btn != ButtonType.OK) return null;
            return new ServicioService.ServicioUpdateBody(
                    Validacion.texto(txtNombre),
                    Validacion.texto(txtDesc),
                    cmbTipo.getValue(),
                    PrecioFormato.parsear(Validacion.texto(txtPrecio)),
                    Estado.aBooleano(cmbEstado.getValue()));
        });
        return dialog;
    }

    /**
     * Construye el pop-up de solo lectura ("Ver detalle").
     *
     * No tiene campos editables: cada dato del servicio se muestra
     * como una etiqueta gris con el valor al lado. El único botón
     * disponible es "Cerrar". Si algún campo viene vacío, se muestra
     * un guión largo (—) en su lugar.
     *
     * @param fila la fila de la tabla con los datos del servicio a mostrar.
     * @return el diálogo listo para mostrar. Su resultado siempre es Void.
     */
    public static Dialog<Void> dialogoVer(ServicioFila fila) {
        Dialog<Void> dialog = baseDialog(
                "Detalle del Servicio", "Información del servicio", 480, 420);

        GridPane grid = grid();
        agregarFilaSoloLectura(grid, 0, "Identificador", fila.getCodigo());
        agregarFilaSoloLectura(grid, 1, "Nombre",        fila.getNombre());
        agregarFilaSoloLectura(grid, 2, "Descripción",   fila.getDescripcion());
        agregarFilaSoloLectura(grid, 3, "Tipo",          fila.getTipo());
        agregarFilaSoloLectura(grid, 4, "Precio",        fila.getPrecioFormateado());
        agregarFilaSoloLectura(grid, 5, "Estado",        fila.getEstado());

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        ((Button) dialog.getDialogPane().lookupButton(ButtonType.CLOSE)).setText("Cerrar");
        return dialog;
    }

    /**
     * Crea la estructura base que comparten los tres diálogos: título
     * en la barra de la ventana, cabecera azul, tamaño preferido y
     * la hoja de estilos CSS aplicada.
     *
     * Los métodos {@link #dialogoCrear}, {@link #dialogoEditar} y
     * {@link #dialogoVer} llaman aquí primero y después agregan los
     * campos y botones específicos de cada uno.
     *
     * @param titulo  texto que aparece en la barra de título del SO.
     * @param header  texto grande dentro de la cabecera azul del diálogo.
     * @param w       ancho preferido del diálogo en píxeles.
     * @param h       alto preferido del diálogo en píxeles.
     * @param <T>     tipo del resultado del diálogo (depende de quién lo use).
     * @return el diálogo con la estructura base, pendiente de agregarle
     *         contenido y botones.
     */
    private static <T> Dialog<T> baseDialog(String titulo, String header, double w, double h) {
        Dialog<T> dialog = new Dialog<>();
        dialog.setTitle(titulo);
        dialog.setHeaderText(header);
        dialog.getDialogPane().getStyleClass().add(CLASE_DIALOGO);
        dialog.getDialogPane().setPrefSize(w, h);
        dialog.getDialogPane().getStylesheets().add(
                ServicioFormulario.class.getResource(STYLESHEET).toExternalForm());
        return dialog;
    }

    /**
     * Valida todos los campos del formulario de creación.
     *
     * Lo hace en este orden:
     *  1. Revisa que el identificador esté presente.
     *  2. Revisa que el identificador tenga el formato correcto
     *     (solo letras, números y guiones).
     *  3. Revisa que el identificador no esté en uso, usando la
     *     función {@code codigoYaExiste} que viene de la vista.
     *  4. Reusa {@link #validarComun} para los demás campos.
     *
     * Si encuentra errores, marca en rojo los campos correspondientes
     * y arma un solo String con todos los mensajes (uno por línea).
     *
     * @return null si todo está bien, o un String con los mensajes
     *         de error agrupados.
     */
    private static String validarCreacion(TextField txtCodigo, TextField txtNombre,
                                          TextField txtDesc, TextField txtPrecio,
                                          ComboBox<String> cmbTipo, ComboBox<String> cmbEstado,
                                          Predicate<String> codigoYaExiste) {
        StringBuilder sb = new StringBuilder();
        String codigo = Validacion.texto(txtCodigo);
        String e1 = Validacion.requerido("Identificador", codigo);
        String e2 = e1 == null ? Validacion.formatoCodigo("Identificador", codigo) : null;
        String e3 = e1 == null && e2 == null && codigoYaExiste.test(codigo)
                ? "• Ya existe un servicio con el identificador '" + codigo + "'"
                : null;
        Validacion.marcarInvalido(txtCodigo, e1 != null || e2 != null || e3 != null);
        if (e1 != null)      sb.append(e1).append("\n");
        else if (e2 != null) sb.append(e2).append("\n");
        else if (e3 != null) sb.append(e3).append("\n");

        String resto = validarComun(txtNombre, txtDesc, txtPrecio, cmbTipo, cmbEstado);
        if (resto != null) sb.append(resto);
        return sb.length() == 0 ? null : sb.toString().trim();
    }

    /**
     * Validaciones comunes para crear y editar.
     *
     * Aplica estas reglas, una por campo:
     *  - Nombre:      obligatorio, máximo 100 caracteres.
     *  - Descripción: obligatoria, máximo 200 caracteres.
     *  - Tipo:        debe haber un valor seleccionado en el ComboBox.
     *  - Precio:      obligatorio, número entero positivo.
     *  - Estado:      debe haber un valor seleccionado en el ComboBox.
     *
     * Antes de revisar limpia las marcas rojas de los campos. Después,
     * por cada regla que falle, marca el campo y agrega el mensaje
     * a la lista.
     *
     * @return null si todos los campos son válidos, o un String con
     *         todos los mensajes de error juntos (uno por línea).
     */
    private static String validarComun(TextField txtNombre, TextField txtDesc,
                                       TextField txtPrecio, ComboBox<String> cmbTipo,
                                       ComboBox<String> cmbEstado) {
        Validacion.limpiarEstado(txtNombre, txtDesc, txtPrecio, cmbTipo, cmbEstado);

        List<Regla> reglas = List.of(
                new Regla(txtNombre, () -> primero(
                        Validacion.requerido("Nombre", Validacion.texto(txtNombre)),
                        Validacion.longitudMax("Nombre", Validacion.texto(txtNombre), 100))),
                new Regla(txtDesc, () -> primero(
                        Validacion.requerido("Descripción", Validacion.texto(txtDesc)),
                        Validacion.longitudMax("Descripción", Validacion.texto(txtDesc), 200))),
                new Regla(cmbTipo, () -> Validacion.comboSeleccionado("un tipo", cmbTipo)),
                new Regla(txtPrecio, () -> primero(
                        Validacion.requerido("Precio", Validacion.texto(txtPrecio)),
                        Validacion.numeroPositivo("Precio", Validacion.texto(txtPrecio)))),
                new Regla(cmbEstado, () -> Validacion.comboSeleccionado("un estado", cmbEstado))
        );

        StringBuilder sb = new StringBuilder();
        for (Regla r : reglas) {
            String error = r.evaluar().get();
            Validacion.marcarInvalido(r.control(), error != null);
            if (error != null) sb.append(error).append("\n");
        }
        return sb.length() == 0 ? null : sb.toString().trim();
    }

    /**
     * Recibe varios mensajes de error y devuelve el primero que no
     * sea null. Sirve para encadenar varias validaciones sobre un
     * mismo campo: solo nos interesa mostrar el primer problema, no
     * todos a la vez.
     *
     * @param mensajes mensajes de error a evaluar en orden.
     * @return el primer mensaje no null, o null si todos lo son.
     */
    private static String primero(String... mensajes) {
        for (String m : mensajes) if (m != null) return m;
        return null;
    }

    /**
     * Empareja un campo (el {@code control}) con la función que lo
     * valida (el {@code evaluar}). Lo usamos en {@link #validarComun}
     * para recorrer todas las reglas en un solo bucle.
     */
    private record Regla(Node control, java.util.function.Supplier<String> evaluar) {}

    /**
     * Crea un TextField con el valor inicial y un texto de ayuda
     * (prompt) que aparece cuando el campo está vacío.
     *
     * @param inicial valor inicial del campo. Si es null se usa "".
     * @param prompt  texto de ayuda que se muestra en gris cuando está vacío.
     * @return el TextField configurado con su estilo y altura.
     */
    private static TextField campoTexto(String inicial, String prompt) {
        TextField tf = new TextField(inicial == null ? "" : inicial);
        tf.setPromptText(prompt);
        tf.getStyleClass().add("campo-form");
        tf.setPrefHeight(36);
        return tf;
    }

    /**
     * Crea el ComboBox del tipo de servicio. Las opciones son las que
     * define {@link ServicioService#TIPOS_SERVICIO} (Consulta, Examen,
     * Procedimiento).
     *
     * @param inicial valor que debe quedar seleccionado al abrir el
     *                ComboBox. Si es null o no está entre las opciones,
     *                queda vacío con el texto de ayuda.
     * @return el ComboBox configurado.
     */
    private static ComboBox<String> comboTipo(String inicial) {
        ComboBox<String> cmb = new ComboBox<>();
        cmb.setMaxWidth(Double.MAX_VALUE);
        cmb.setPrefHeight(36);
        cmb.setPromptText("Selecciona un tipo");
        cmb.getItems().addAll(ServicioService.TIPOS_SERVICIO);
        if (inicial != null && cmb.getItems().contains(inicial)) cmb.setValue(inicial);
        cmb.getStyleClass().add("campo-form");
        return cmb;
    }

    /**
     * Crea el ComboBox del estado del servicio. Las opciones son las
     * dos constantes de {@link Estado}: Activo e Inactivo.
     *
     * @param inicial texto que queda seleccionado al abrir el ComboBox.
     * @return el ComboBox configurado.
     */
    private static ComboBox<String> comboEstado(String inicial) {
        ComboBox<String> cmb = new ComboBox<>();
        cmb.setMaxWidth(Double.MAX_VALUE);
        cmb.setPrefHeight(36);
        cmb.getItems().addAll(Estado.ACTIVO, Estado.INACTIVO);
        cmb.setValue(inicial);
        cmb.getStyleClass().add("campo-form");
        return cmb;
    }

    /**
     * Crea el GridPane (cuadrícula) base donde se acomodan los
     * campos del formulario.
     *
     * Tiene dos columnas:
     *  - La izquierda es para las etiquetas ("Nombre", "Tipo", etc.),
     *    alineadas a la derecha y con un ancho mínimo de 120 px para
     *    que queden parejas.
     *  - La derecha es para los inputs (TextField, ComboBox) y crece
     *    para llenar el ancho del diálogo.
     *
     * @return el GridPane vacío, listo para ir agregándole filas.
     */
    private static GridPane grid() {
        GridPane grid = new GridPane();
        grid.setHgap(14);
        grid.setVgap(14);
        grid.setPadding(new Insets(22, 24, 18, 24));
        ColumnConstraints c0 = new ColumnConstraints();
        c0.setMinWidth(120);
        c0.setHalignment(HPos.RIGHT);
        ColumnConstraints c1 = new ColumnConstraints();
        c1.setHgrow(Priority.ALWAYS);
        c1.setFillWidth(true);
        grid.getColumnConstraints().addAll(c0, c1);
        return grid;
    }

    /**
     * Agrega una fila al GridPane con su etiqueta a la izquierda y
     * el control de entrada a la derecha.
     *
     * @param grid     el GridPane donde se va a agregar la fila.
     * @param fila     número de fila (empezando desde 0).
     * @param etiqueta texto que se muestra a la izquierda (ej: "Nombre").
     * @param control  el componente de entrada: TextField, ComboBox, etc.
     */
    private static void agregarFila(GridPane grid, int fila, String etiqueta, Node control) {
        Label lbl = new Label(etiqueta);
        lbl.getStyleClass().add("etiqueta-form");
        grid.add(lbl, 0, fila);
        grid.add(control, 1, fila);
        if (control instanceof Region r) GridPane.setHgrow(r, Priority.ALWAYS);
    }

    /**
     * Agrega una fila al GridPane pero en modo de solo lectura: la
     * etiqueta a la izquierda y el valor como texto normal (no como
     * input) a la derecha. Se usa en el diálogo de "Ver detalle".
     *
     * Si el valor llega null o vacío, se muestra un guión largo (—)
     * para que no quede una línea en blanco.
     *
     * @param grid     el GridPane donde se va a agregar la fila.
     * @param fila     número de fila (empezando desde 0).
     * @param etiqueta texto a la izquierda.
     * @param valor    texto a la derecha (no editable).
     */
    private static void agregarFilaSoloLectura(GridPane grid, int fila, String etiqueta, String valor) {
        Label lbl = new Label(etiqueta);
        lbl.getStyleClass().add("etiqueta-form");
        Label val = new Label(valor == null || valor.isBlank() ? "—" : valor);
        val.getStyleClass().add("valor-solo-lectura");
        val.setWrapText(true);
        grid.add(lbl, 0, fila);
        grid.add(val, 1, fila);
        GridPane.setHgrow(val, Priority.ALWAYS);
    }

    /**
     * Crea el Label rojo donde se muestran los mensajes de error al
     * intentar guardar con datos inválidos. Empieza oculto: solo
     * aparece cuando hay algo que mostrar.
     *
     * @return el Label configurado e inicialmente invisible.
     */
    private static Label etiquetaErrores() {
        Label lbl = new Label();
        lbl.getStyleClass().add("errores-form");
        lbl.setVisible(false);
        lbl.setManaged(false);
        lbl.setWrapText(true);
        return lbl;
    }

    /**
     * Muestra los mensajes de error dentro del propio formulario,
     * en el Label rojo creado por {@link #etiquetaErrores()}.
     *
     * @param lbl     el Label de errores del formulario.
     * @param mensaje texto con todos los errores juntos.
     */
    private static void mostrarErroresInline(Label lbl, String mensaje) {
        lbl.setText(mensaje);
        lbl.setVisible(true);
        lbl.setManaged(true);
    }
}
