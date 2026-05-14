package services.http;

import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
/**
 * Contrato base para servicios HTTP genéricos.
 *
 * <p>
 * Define operaciones CRUD básicas utilizando programación
 * asíncrona mediante {@link CompletableFuture}.
 * </p>
 *
 * @param <T> tipo de entidad manejada por el servicio
 * @param <ID> tipo del identificador de la entidad
 */
public interface IHttpService <T, ID>{

    /**
     * Obtiene todos los recursos disponibles.
     *
     * @return un {@link CompletableFuture} con la respuesta
     *         del servidor en formato JSON
     */
    CompletableFuture<String> getAll();

    /**
     * Obtiene un recurso específico mediante su identificador.
     *
     * @param id identificador del recurso
     * @return un {@link CompletableFuture} con la respuesta
     *         del servidor en formato JSON
     */
    CompletableFuture<String> getById(ID id);

    /**
     * Envía una petición para crear un nuevo recurso.
     *
     * @param body objeto que será enviado en el cuerpo de la petición
     * @return un {@link CompletableFuture} con la respuesta del servidor
     */
    CompletableFuture<String> post(T body);

    /**
     * Envía una petición para actualizar un recurso existente.
     *
     * @param id identificador del recurso a actualizar
     * @param body objeto con la información actualizada
     * @return un {@link CompletableFuture} con la respuesta del servidor
     */
    CompletableFuture<String> put(ID id, T body);

    /**
     * Valida la respuesta HTTP recibida desde el servidor.
     *
     * @param response respuesta HTTP obtenida
     * @throws Exception si la respuesta contiene un error HTTP
     */
    void validateResponse(HttpResponse<String> response) throws Exception;
}
