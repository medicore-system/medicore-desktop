package views.servicio;

import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import models.ServicioModel;
import services.ServicioService;
import views.common.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Vista principal del módulo de Servicios, la que se ve cuando uno
 * entra a "Servicios" desde el menú lateral.
 *
 * Esta clase es básicamente un "coordinador". No hace las cosas
 * pesadas por sí misma, sino que delega:
 *  - {@link ServicioService} le entrega los datos (mock con JSON).
 *  - {@link ConvertidorServicio} convierte esos datos al formato de
 *    la tabla.
 *  - {@link TablaServicios} construye la TableView.
 *  - {@link ServicioBuscador} filtra la lista cuando el usuario
 *    escribe en el buscador.
 *  - {@link ServicioFormulario} construye los pop-ups de crear,
 *    editar y ver.
 *  - {@link Toast} muestra las notificaciones verdes y rojas.
 *
 * Esta clase solo se encarga de organizar el layout, conectar los
 * eventos y disparar las tareas en hilos de fondo para que la
 * pantalla no se congele.
 */
public class ServicioView extends VBox {

    private static final String STYLESHEET = "/styles/servicio/servicio.css";

    private final ServicioService servicioService = new ServicioService();
    private List<ServicioFila> todosLosServicios = new ArrayList<>();

    private Label titulo;
    private TextField buscador;
    private Label lblContador;
    private Button btnNuevoServicio;
    private TableView<ServicioFila> tabla;

    /**
     * Construye la vista de servicios.
     *
     * Lo hace en este orden:
     *  1. Crea todos los componentes (botones, campos, tabla).
     *  2. Los acomoda en pantalla (layout).
     *  3. Conecta los eventos (clics, escritura en el buscador).
     *  4. Aplica los estilos CSS.
     *  5. Dispara la carga inicial de servicios en un hilo de fondo.
     */
    public ServicioView() {
        iniciarComponentes();
        configurarLayout();
        registrarEventos();
        cargarEstilos();
        cargarServicios();
    }

    /**
     * Crea los componentes visuales individuales: el título, el
     * buscador, el contador de resultados, el botón "Nuevo Servicio"
     * y la tabla.
     *
     * La tabla se construye llamando a {@link TablaServicios#crear},
     * pasándole los métodos {@link #abrirDialogoVer} y
     * {@link #abrirDialogoEditar} como callbacks de los botones de
     * acción de cada fila.
     *
     * Este método solo CREA los componentes; el acomodo en pantalla
     * lo hace {@link #configurarLayout}.
     */
    private void iniciarComponentes() {
        titulo = new Label("Gestión de Servicios");
        titulo.getStyleClass().add("titulo");

        buscador = new TextField();
        buscador.setPromptText("🔍  Buscar servicio...");
        buscador.setId("buscador");

        lblContador = new Label("");
        lblContador.getStyleClass().add("contador-resultados");

        btnNuevoServicio = new Button("+ Nuevo Servicio");
        btnNuevoServicio.setId("btnNuevoServicio");

        tabla = TablaServicios.crear(this::abrirDialogoVer, this::abrirDialogoEditar);
    }

    /**
     * Organiza los componentes en la pantalla en tres "tiras"
     * verticales:
     *   1. Barra de título: a la izquierda el título, a la derecha
     *      el botón "+ Nuevo Servicio".
     *   2. Barra de búsqueda: el buscador y el contador "N servicios".
     *   3. La tabla, ocupando todo el espacio que sobre.
     */
    private void configurarLayout() {
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox barraTitulo = new HBox(titulo, spacer, btnNuevoServicio);
        barraTitulo.setAlignment(Pos.CENTER_LEFT);

        HBox barraBusqueda = new HBox(12, buscador, lblContador);
        barraBusqueda.setAlignment(Pos.CENTER_LEFT);

        setSpacing(15);
        setPadding(new Insets(24));
        getChildren().addAll(barraTitulo, barraBusqueda, tabla);
        VBox.setVgrow(tabla, Priority.ALWAYS);
    }

    /**
     * Conecta los eventos de los componentes interactivos:
     *  - Cada vez que el usuario escribe (o borra) en el buscador,
     *    se ejecuta {@link #aplicarBusqueda} para refiltrar la tabla.
     *  - Cuando el usuario presiona "+ Nuevo Servicio" se abre el
     *    diálogo de creación.
     */
    private void registrarEventos() {
        buscador.textProperty().addListener((obs, anterior, texto) -> aplicarBusqueda(texto));
        btnNuevoServicio.setOnAction(e -> abrirDialogoCrear());
    }

    /**
     * Aplica la hoja de estilos servicio.css a esta vista y le agrega
     * la clase {@code "servicio-view"} para que el CSS pueda apuntar
     * al fondo y al resto de elementos.
     */
    private void cargarEstilos() {
        getStyleClass().add("servicio-view");
        getStylesheets().add(getClass().getResource(STYLESHEET).toExternalForm());
    }

    /**
     * Pide la lista de servicios al {@link ServicioService} en un
     * hilo aparte para que la pantalla no se congele mientras carga.
     *
     * Cuando el hilo termina:
     *  - Si salió bien, guarda la lista en {@link #todosLosServicios}
     *    y aplica la búsqueda actual del buscador (para mantener el
     *    filtro si el usuario tenía algo escrito).
     *  - Si falla, muestra un cuadro de error.
     */
    private void cargarServicios() {
        Task<List<ServicioFila>> task = new Task<>() {
            @Override
            protected List<ServicioFila> call() {
                return ConvertidorServicio.aFilas(servicioService.getAll());
            }
        };
        task.setOnSucceeded(e -> {
            todosLosServicios = new ArrayList<>(task.getValue());
            aplicarBusqueda(buscador.getText());
        });
        task.setOnFailed(e -> mostrarError("No se pudieron cargar los servicios."));
        new Thread(task).start();
    }

    /**
     * Filtra la tabla según el texto que el usuario tenga en el
     * buscador y actualiza el contador de la derecha.
     *
     * El filtrado real lo hace {@link ServicioBuscador#buscar}; aquí
     * solo recibimos el resultado y lo pintamos.
     *
     * @param texto lo que hay en el buscador. Puede ser null o vacío
     *              (en ese caso se muestran todos los servicios).
     */
    private void aplicarBusqueda(String texto) {
        List<ServicioFila> resultados = ServicioBuscador.buscar(todosLosServicios, texto);
        tabla.setItems(FXCollections.observableArrayList(resultados));
        actualizarContador(resultados.size(), todosLosServicios.size());
    }

    /**
     * Cambia el texto del contador según cuántos servicios se están
     * mostrando.
     *
     * Reglas:
     *  - Si se muestran todos: "N servicios" (o "1 servicio" en
     *    singular).
     *  - Si hay un filtro activo: "X de N servicios".
     *
     * @param mostrados cantidad de servicios que pasaron el filtro.
     * @param total     cantidad total de servicios en memoria.
     */
    private void actualizarContador(int mostrados, int total) {
        if (mostrados == total) {
            lblContador.setText(total + (total == 1 ? " servicio" : " servicios"));
        } else {
            lblContador.setText(mostrados + " de " + total + " servicios");
        }
    }

    /**
     * Abre el pop-up para crear un servicio nuevo. Si el usuario
     * confirma, se llama a {@link #crearServicio} con los datos del
     * formulario.
     */
    private void abrirDialogoCrear() {
        Dialog<ServicioService.ServicioCreateBody> dialog =
                ServicioFormulario.dialogoCrear(this::existeCodigo);
        dialog.showAndWait().ifPresent(this::crearServicio);
    }

    /**
     * Abre el pop-up de edición precargado con los datos del servicio
     * de la fila que el usuario presionó. Si confirma los cambios, se
     * llama a {@link #actualizarServicio}.
     *
     * @param fila la fila de la tabla cuya edición se solicitó.
     */
    private void abrirDialogoEditar(ServicioFila fila) {
        Dialog<ServicioService.ServicioUpdateBody> dialog = ServicioFormulario.dialogoEditar(fila);
        dialog.showAndWait().ifPresent(body -> actualizarServicio(fila.getCodigo(), body));
    }

    /**
     * Abre el pop-up de solo lectura con la información del servicio.
     *
     * @param fila la fila de la tabla seleccionada.
     */
    private void abrirDialogoVer(ServicioFila fila) {
        ServicioFormulario.dialogoVer(fila).showAndWait();
    }

    /**
     * Envía la solicitud de creación al {@link ServicioService} en un
     * hilo aparte. Cuando termina:
     *  - Si salió bien: muestra un toast verde y recarga la tabla
     *    para que aparezca el servicio nuevo.
     *  - Si falló (por ejemplo si el código ya existía): muestra un
     *    toast rojo y un cuadro de error.
     *
     * @param body los datos del servicio que vienen del formulario.
     */
    private void crearServicio(ServicioService.ServicioCreateBody body) {
        Task<ServicioModel> task = new Task<>() {
            @Override
            protected ServicioModel call() throws Exception {
                return servicioService.create(body);
            }
        };
        task.setOnSucceeded(e -> {
            Toast.success(this, "Servicio '" + body.nombre() + "' creado correctamente");
            cargarServicios();
        });
        task.setOnFailed(e -> {
            Toast.error(this, "Error al crear el servicio");
            mostrarError("Error al crear el servicio.\nVerifica que el identificador sea único.");
        });
        new Thread(task).start();
    }

    /**
     * Envía la solicitud de actualización al {@link ServicioService}
     * en un hilo aparte. Funciona igual que {@link #crearServicio}
     * pero llamando a {@code update} y notificando con el mensaje
     * de "actualizado".
     *
     * @param codigo identificador del servicio a actualizar.
     * @param body   datos nuevos que vienen del formulario.
     */
    private void actualizarServicio(String codigo, ServicioService.ServicioUpdateBody body) {
        Task<ServicioModel> task = new Task<>() {
            @Override
            protected ServicioModel call() throws Exception {
                return servicioService.update(codigo, body);
            }
        };
        task.setOnSucceeded(e -> {
            Toast.success(this, "Servicio '" + body.nombre() + "' actualizado correctamente");
            cargarServicios();
        });
        task.setOnFailed(e -> {
            Toast.error(this, "Error al actualizar el servicio");
            mostrarError("Error al actualizar el servicio.");
        });
        new Thread(task).start();
    }

    /**
     * Revisa en la lista que tenemos cargada actualmente si ya existe
     * un servicio con el código dado. Este método es el que se le
     * pasa a {@link ServicioFormulario#dialogoCrear} para validar la
     * unicidad antes de enviar.
     *
     * Compara sin distinguir mayúsculas de minúsculas.
     *
     * @param codigo código a revisar.
     * @return true si ya hay un servicio con ese código; false si está libre.
     */
    private boolean existeCodigo(String codigo) {
        if (codigo == null || codigo.isBlank()) return false;
        return todosLosServicios.stream()
                .anyMatch(s -> codigo.equalsIgnoreCase(s.getCodigo()));
    }

    /**
     * Muestra un cuadro de diálogo de error nativo con el mensaje
     * que le pasemos. Se usa cuando una operación falla de forma
     * grave y el toast no es suficiente.
     *
     * @param mensaje texto a mostrar en el cuadro.
     */
    private void mostrarError(String mensaje) {
        new Alert(Alert.AlertType.ERROR, mensaje, ButtonType.OK).showAndWait();
    }
}
