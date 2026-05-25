package services;

import com.fasterxml.jackson.core.type.TypeReference;
import models.CitaModel;
import models.UsuarioModel;
import services.http.HttpServiceImpl;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class CitaService extends HttpServiceImpl<Object, String> {
    private static final String BASE_URL = "http://localhost:8080/Citas";
    private static CitaService instance;

    private CitaService(){
        super(BASE_URL);
    }

    public static CitaService getInstance(){
        if(instance == null) instance = new CitaService();
        return instance;
    }

    /**
     * Obtiene Todas las citas por el documento del usuario.
     *
     * @param documento Documento de identificación del usuario.
     * @return Usuario encontrado.
     * @throws Exception si no existe o el servidor falla.
     */
    public CompletableFuture<List<CitaModel>> getCitasPaciente(String documento){
        return getById(documento).thenApply(json -> {
            try{
                return mapper.readValue(json, new TypeReference<List<CitaModel>>(){});
            }catch (Exception e){
                throw new RuntimeException("Error Parseando Citas: " + e.getMessage());
            }
        });
    }

    /**
     * Cambia el estado de la cita a 'APROBADA'.
     *
     * @param codigo codigo de la cita.
     * @return Cita con el estado actualizado.
     * @throws Exception si el servidor responde con error.
     */
    public CompletableFuture<CitaModel> aprobar(String codigo) {
        return putCustom(BASE_URL + "/aprobar/" + codigo, null)
                .thenApply(json -> {
                    try{
                        return mapper.readValue(json, CitaModel.class);

                    }catch (Exception e){
                        throw new RuntimeException("Error parseando respuesta: " + e.getMessage(), e);
                    }
                });
    }

    /**
     * Cambia el estado de la cita a 'DENEGADA'.
     *
     * @param codigo codigo de la cita.
     * @return Cita con el estado actualizado.
     * @throws Exception si el servidor responde con error.
     */
    public CompletableFuture<CitaModel> denegar(String codigo) {
        return putCustom(BASE_URL + "/denegar/" + codigo, null)
                .thenApply(json -> {
                    try{
                        return mapper.readValue(json, CitaModel.class);

                    }catch (Exception e){
                        throw new RuntimeException("Error parseando respuesta: " + e.getMessage(), e);
                    }
                });
    }
}
