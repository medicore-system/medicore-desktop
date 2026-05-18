package services;

import com.fasterxml.jackson.core.type.TypeReference;
import models.UsuarioModel;
import services.http.HttpServiceImpl;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Servicio HTTP encargado de gestionar las operaciones
 * relacionadas con los usuarios del sistema.
 *
 * <p>
 * Todos los métodos son síncronos, por lo que las llamadas
 * deben ejecutarse dentro de un {@code Task} de JavaFX
 * para evitar bloquear el hilo principal de la interfaz.
 * </p>
 *
 * <h2>Endpoints cubiertos</h2>
 * <ul>
 *     <li>GET /Usuarios</li>
 *     <li>GET /Usuarios/{documento}</li>
 *     <li>POST /Usuarios</li>
 *     <li>PUT /Usuarios/{documento}</li>
 *     <li>PUT /Usuarios/inhabilitar/{documento}</li>
 * </ul>
 *
 * <p>
 * Implementa el patrón Singleton para mantener una única
 * instancia compartida en toda la aplicación.
 * </p>
 */
public class UsuarioService extends HttpServiceImpl<Object, String> {

    /**
     * URL base de la API para Usuarios.
     */
    private static final String BASE_URL = "http://localhost:8080/Usuarios";

    /**
     * Instancia única del servicio.
     */

    private static UsuarioService instance;

    /**
     * Constructor privado para evitar instanciación externa.
     */
    private UsuarioService() {
        super(BASE_URL);
    }

    /**
     * Obtiene la instancia única del servicio.
     *
     * @return instancia singleton de {@code UsuarioService}
     */
    public static UsuarioService getInstance() {
        if (instance == null) instance = new UsuarioService();
        return instance;
    }


    /**
     * Obtiene todos los usuarios registrados en el sistema.
     *
     * @return lista de usuarios obtenidos desde el backend
     * @throws Exception si ocurre un error durante la petición
     *                   o el servidor responde con error HTTP
     */
    public CompletableFuture<List<UsuarioModel>> getAllUsers() {
        return getAll().thenApply(json -> {
            try{
                return mapper.readValue(json, new TypeReference<List<UsuarioModel>>() {});
            }catch (Exception e){
                throw new RuntimeException("Error Parseando Usuarios: " + e.getMessage());
            }
        });
    }

    /**
     * Obtiene un usuario por su documento.
     *
     * @param documento Documento de identificación del usuario.
     * @return Usuario encontrado.
     * @throws Exception si no existe o el servidor falla.
     */
    public CompletableFuture<UsuarioModel> getUsersByDoc(String documento){
        return getById(documento).thenApply(json -> {
            try{
                return mapper.readValue(json, UsuarioModel.class);
            }catch (Exception e){
                throw new RuntimeException("Error Parseando Usuario: " + e.getMessage());
            }
        });
    }

    /**
     * Registra un nuevo usuario en el sistema.
     *
     * @param body Datos del usuario a crear.
     * @return Usuario creado con los datos asignados por el backend.
     * @throws Exception si el servidor responde con error.
     */
    public CompletableFuture<UsuarioModel> crear(UsuarioCreateBody body) {
        return post(body).thenApply(json -> {
            try{
                return mapper.readValue(json, UsuarioModel.class);
            }catch (Exception e){
                throw new RuntimeException("Error parseando respuesta: " + e.getMessage(), e);
            }
        });
    }

    /**
     * Actualiza los datos de un usuario existente.
     *
     * @param documento Documento del usuario a actualizar.
     * @param body      Nuevos datos del usuario.
     * @return Usuario actualizado.
     * @throws Exception si el servidor responde con error.
     */
    public CompletableFuture<UsuarioModel> actualizar(String documento, UsuarioUpdateBody body){
        return put(documento, body).thenApply(json -> {
            try{
                return mapper.readValue(json, UsuarioModel.class);
            }catch (Exception e){
                throw new RuntimeException("Error parseando respuesta: " + e.getMessage(), e);
            }
        });
    }

    /**
     * Cambia el estado del usuario (habilitar/inhabilitar).
     * El backend togglea automáticamente el estado actual.
     *
     * @param documento Documento del usuario.
     * @return Usuario con el estado actualizado.
     * @throws Exception si el servidor responde con error.
     */
    public CompletableFuture<UsuarioModel> toggleEstado(String documento)  {
        return putCustom(BASE_URL + "/inhabilitar/" + documento, null)
                .thenApply(json -> {
                    try{
                        return mapper.readValue(json, UsuarioModel.class);
                    }catch (Exception e){
                        System.out.println(e.getMessage());
                        throw new RuntimeException("Error parseando respuesta: " + e.getMessage(), e);
                    }
                });
    }

    /**
     * Cuerpo de la petición POST /Usuarios.
     * Mapea exactamente UsuarioCreateRequestDTO del backend.
     */
    public record UsuarioCreateBody(
            String documento,
            String nombre,
            String apellido,
            String correo,
            String telefono,
            String codigo_eps,
            String codigo_ciudad
    ) {}

    /**
     * Cuerpo de la petición PUT /Usuarios/{documento}.
     * Mapea exactamente UsuarioUpdateRequestDTO del backend.
     */
    public record UsuarioUpdateBody(
            String nombre,
            String apellido,
            String telefono,
            String codigo_eps,
            String codigo_ciudad
    ) {}
}
