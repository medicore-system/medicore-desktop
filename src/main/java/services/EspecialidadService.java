package services;

import com.fasterxml.jackson.core.type.TypeReference;
import models.EspecialidadModel;
import models.MedicoModel;
import services.http.HttpServiceImpl;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class EspecialidadService extends HttpServiceImpl<Object, String> {

    private static final String BASE_URL = "http://localhost:8080/especialidad";
    private static EspecialidadService instance;

    private EspecialidadService(){super(BASE_URL);}

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
