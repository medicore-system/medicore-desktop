package services;

import com.fasterxml.jackson.core.type.TypeReference;
import models.EspecialidadModel;
import services.http.HttpServiceImpl;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Servicio encargado de gestionar las operaciones relacionadas
 * con las especialidades médicas.
 *
 * Permite obtener información de las especialidades registradas
 * desde el backend mediante peticiones HTTP.
 */
public class EspecialidadService extends HttpServiceImpl<Object, String> {

    /**
     * URL base del endpoint de especialidades.
     */
    private static final String BASE_URL = "http://localhost:8080/especialidad";

    /**
     * Instancia única del servicio.
     */
    private static EspecialidadService instance;

    /**
     * Constructor privado para aplicar el patrón Singleton.
     */
    private EspecialidadService(){
        super(BASE_URL);
    }

    /**
     * Obtiene la instancia única del servicio.
     *
     * @return instancia de EspecialidadService
     */
    public static EspecialidadService getInstance(){
        if(instance == null) instance = new EspecialidadService();
        return instance;
    }

    /**
     * Obtiene la lista de todos las especialidades registradas.
     *
     * @return CompletableFuture con la lista de médicos
     */
    public CompletableFuture<List<EspecialidadModel>> getAllEspecialidades() {
        return getAll().thenApply(json -> {
            try{
                return mapper.readValue(json, new TypeReference<List<EspecialidadModel>>() {});
            }catch (Exception e){
                throw new RuntimeException("Error Parseando Especialidades: " + e.getMessage());
            }
        });
    }
}
