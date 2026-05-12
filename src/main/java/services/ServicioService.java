package services;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import models.ServicioModel;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Es la clase que se encarga de "ir a buscar" los servicios.
 *
 * Como el backend del compañero todavía no está listo, por ahora los
 * datos vienen de un JSON quemado escrito aquí mismo. Lo leemos con
 * Gson y lo convertimos en objetos {@link ServicioModel}, igual que
 * como llegarían desde el servidor real.
 *
 * Cuando el back esté funcionando, solo hay que cambiar el cuerpo
 * de los métodos para que hagan peticiones HTTP en lugar de leer el
 * JSON; la vista no se entera del cambio porque los métodos siguen
 * recibiendo y devolviendo lo mismo.
 *
 * Los métodos son bloqueantes (se quedan esperando la respuesta),
 * así que la vista siempre los llama dentro de un {@code Task} de
 * JavaFX para que la pantalla no se congele.
 */
public class ServicioService {

    /**
     * Lista de tipos válidos que se pueden seleccionar en el ComboBox
     * de "Tipo" del formulario. Se expone como constante para que el
     * formulario y la validación usen exactamente los mismos valores.
     */
    public static final List<String> TIPOS_SERVICIO =
            List.of("Consulta", "Examen", "Procedimiento");

    /**
     * Almacén de servicios en memoria. Funciona como una "mini base
     * de datos" que vive mientras la app está abierta: lo que se cree
     * o se actualice queda guardado aquí hasta que el usuario cierre
     * la aplicación.
     *
     * Es static para que todas las instancias de {@code ServicioService}
     * vean los mismos datos, y synchronized para que sea seguro tocarlo
     * desde varios hilos a la vez (porque la UI corre tareas en hilos
     * de fondo con {@code Task}).
     */
    private static final List<ServicioModel> STORE = cargarDatosIniciales();

    /**
     * JSON quemado con los servicios iniciales. Lo dejamos como un
     * String para usar Gson igual que lo usaríamos con la respuesta
     * real del backend.
     */
    private static final String JSON_SERVICIOS = """
            [
              {
                "codigo": "MED-001",
                "nombre": "Consulta Médica General",
                "descripcion": "Consulta diagnóstica integral de atención primaria",
                "tipo": "Consulta",
                "precio": 80000,
                "estado": true
              },
              {
                "codigo": "EXA-005",
                "nombre": "Examen de Laboratorio",
                "descripcion": "Análisis de sangre y orina estándar",
                "tipo": "Examen",
                "precio": 45000,
                "estado": true
              },
              {
                "codigo": "PROC-012",
                "nombre": "Procedimiento Menor",
                "descripcion": "Sutura y curación de heridas superficiales",
                "tipo": "Procedimiento",
                "precio": 120000,
                "estado": false
              },
              {
                "codigo": "MED-002",
                "nombre": "Consulta Especializada",
                "descripcion": "Consulta con médico especialista",
                "tipo": "Consulta",
                "precio": 150000,
                "estado": true
              },
              {
                "codigo": "EXA-010",
                "nombre": "Radiografía Simple",
                "descripcion": "Toma e interpretación de radiografía convencional",
                "tipo": "Examen",
                "precio": 95000,
                "estado": true
              }
            ]
            """;

    /**
     * Devuelve la lista completa de servicios registrados, sin filtrar
     * por estado (incluye activos e inactivos).
     *
     * Equivale al endpoint {@code GET /services} del swagger.
     *
     * Devuelve una copia nueva del almacén para que quien la reciba
     * pueda modificar la lista sin afectar los datos guardados.
     *
     * @return lista con todos los servicios actuales.
     */
    public List<ServicioModel> getAll() {
        return new ArrayList<>(STORE);
    }

    /**
     * Busca un servicio específico por su código.
     *
     * Equivale al endpoint {@code GET /services/{id}} del swagger.
     *
     * @param codigo identificador del servicio (ej: "MED-001"). No
     *               distingue mayúsculas de minúsculas.
     * @return el servicio encontrado.
     * @throws Exception si no existe ningún servicio con ese código.
     */
    public ServicioModel getById(String codigo) throws Exception {
        return buscarPorCodigo(codigo)
                .orElseThrow(() -> new Exception("Servicio no encontrado: " + codigo));
    }

    /**
     * Agrega un servicio nuevo al almacén.
     *
     * Equivale al endpoint {@code POST /services} del swagger.
     *
     * Antes de agregarlo revisa que no exista otro servicio con el
     * mismo código (la unicidad la garantiza este método, no la vista).
     *
     * @param body datos del nuevo servicio que vienen del formulario.
     * @return el servicio recién creado.
     * @throws Exception si ya existe un servicio con ese código.
     */
    public ServicioModel create(ServicioCreateBody body) throws Exception {
        if (buscarPorCodigo(body.codigo()).isPresent()) {
            throw new Exception("Ya existe un servicio con el código " + body.codigo());
        }
        ServicioModel nuevo = new ServicioModel();
        nuevo.codigo      = body.codigo();
        nuevo.nombre      = body.nombre();
        nuevo.descripcion = body.descripcion();
        nuevo.tipo        = body.tipo();
        nuevo.precio      = body.precio();
        nuevo.estado      = body.estado();
        STORE.add(nuevo);
        return nuevo;
    }

    /**
     * Cambia los datos de un servicio que ya existe.
     *
     * Equivale al endpoint {@code PUT /services/{id}} del swagger.
     *
     * El código del servicio NO se puede cambiar; lo demás (nombre,
     * descripción, tipo, precio, estado) se sobrescribe completo con
     * lo que venga en {@code body}.
     *
     * @param codigo identificador del servicio que se va a editar.
     * @param body   datos nuevos del servicio.
     * @return el servicio ya actualizado.
     * @throws Exception si no existe un servicio con ese código.
     */
    public ServicioModel update(String codigo, ServicioUpdateBody body) throws Exception {
        ServicioModel existente = buscarPorCodigo(codigo)
                .orElseThrow(() -> new Exception("Servicio no encontrado: " + codigo));
        existente.nombre      = body.nombre();
        existente.descripcion = body.descripcion();
        existente.tipo        = body.tipo();
        existente.precio      = body.precio();
        existente.estado      = body.estado();
        return existente;
    }

    /**
     * Desactiva un servicio. NO lo borra físicamente: solo cambia su
     * estado a inactivo para que no aparezca como disponible pero se
     * conserve el historial.
     *
     * Equivale al endpoint {@code DELETE /services/{id}} del swagger.
     *
     * @param codigo identificador del servicio a desactivar.
     * @throws Exception si no existe un servicio con ese código.
     */
    public void delete(String codigo) throws Exception {
        ServicioModel existente = buscarPorCodigo(codigo)
                .orElseThrow(() -> new Exception("Servicio no encontrado: " + codigo));
        existente.estado = false;
    }

    /**
     * Se ejecuta una sola vez, cuando Java carga la clase. Lee el
     * JSON quemado con Gson y deja la lista lista para usar.
     *
     * Envolvemos la lista en {@link Collections#synchronizedList} para
     * que sea segura cuando se accede desde varios hilos (las tareas
     * de fondo de JavaFX corren en hilos distintos al de la UI).
     *
     * @return lista inicial de servicios para guardar en {@link #STORE}.
     */
    private static List<ServicioModel> cargarDatosIniciales() {
        List<ServicioModel> iniciales = new Gson().fromJson(
                JSON_SERVICIOS,
                new TypeToken<List<ServicioModel>>() {}.getType()
        );
        return iniciales == null
                ? Collections.synchronizedList(new ArrayList<>())
                : Collections.synchronizedList(new ArrayList<>(iniciales));
    }

    /**
     * Busca un servicio en el almacén por su código, ignorando si está
     * en mayúsculas o minúsculas. Es el método "interno" que usan
     * {@link #getById}, {@link #update} y {@link #delete}.
     *
     * @param codigo código a buscar.
     * @return {@link Optional} con el servicio si lo encuentra, o vacío si no.
     */
    private Optional<ServicioModel> buscarPorCodigo(String codigo) {
        if (codigo == null) return Optional.empty();
        return STORE.stream()
                .filter(s -> codigo.equalsIgnoreCase(s.codigo))
                .findFirst();
    }

    /**
     * Datos que se envían al crear un servicio. Es como el
     * {@code ServiceRequest} que define el swagger, pero como record
     * de Java (clase inmutable y compacta).
     */
    public record ServicioCreateBody(String codigo, String nombre, String descripcion,
                                     String tipo, BigDecimal precio, Boolean estado) {}

    /**
     * Datos que se envían al actualizar un servicio. Igual que
     * {@link ServicioCreateBody} pero sin el código (porque al editar
     * el código va en la URL, no en el cuerpo).
     */
    public record ServicioUpdateBody(String nombre, String descripcion,
                                     String tipo, BigDecimal precio, Boolean estado) {}
}
