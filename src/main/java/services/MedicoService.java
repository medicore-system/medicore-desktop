package services;

import com.fasterxml.jackson.core.type.TypeReference;
import models.MedicoModel;
import services.http.HttpServiceImpl;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Servicio encargado de gestionar las operaciones relacionadas con los médicos.
 * Permite consultar, crear, actualizar e inhabilitar médicos mediante
 * peticiones HTTP al backend.
 *
 * Extiende la clase HttpServiceImpl para reutilizar las operaciones HTTP básicas.
 *
 * @author kAmii
 */
public class MedicoService extends HttpServiceImpl<Object, String> {

    /**
     * URL base del servicio de médicos.
     */
    private static final String BASE_URL = "http://localhost:8080/medicos";

    /**
     * Instancia única del servicio.
     */
    private static MedicoService instance;

    /**
     * Constructor privado para implementar el patrón Singleton.
     */
    private MedicoService() {
        super(BASE_URL);
    }

    /**
     * Obtiene la instancia única del servicio.
     *
     * @return instancia de MedicoService
     */
    public static MedicoService getInstance(){
        if(instance == null) instance = new MedicoService();
        return instance;
    }

    /**
     * Obtiene la lista de todos los médicos registrados.
     *
     * @return CompletableFuture con la lista de médicos
     */
    public CompletableFuture<List<MedicoModel>> getAllMedicos() {
        return getAll().thenApply(json -> {
            try{
                return mapper.readValue(json, new TypeReference<List<MedicoModel>>() {});
            }catch (Exception e){
                throw new RuntimeException("Error Parseando Medicos: " + e.getMessage());
            }
        });
    }

    /**
     * Obtiene la lista de todos los médicos registrados que esten activos.
     *
     * @return CompletableFuture con la lista de médicos
     */
    public CompletableFuture<List<MedicoModel>> getAllMedicosActivos() {
        return getAllCustom("/activos").thenApply(json -> {
            try{
                return mapper.readValue(json, new TypeReference<List<MedicoModel>>() {});
            }catch (Exception e){
                throw new RuntimeException("Error Parseando Medicos: " + e.getMessage());
            }
        });
    }

    /**
     * Obtiene un médico a partir de su documento.
     *
     * @param documento documento del médico a consultar
     * @return CompletableFuture con el médico encontrado
     */
    public CompletableFuture<MedicoModel> getUsersByDoc(String documento){
        return getById(documento).thenApply(json -> {
            try{
                return mapper.readValue(json, MedicoModel.class);
            }catch (Exception e){
                throw new RuntimeException("Error Parseando Medicos: " + e.getMessage());
            }
        });
    }

    /**
     * Crea un nuevo médico en el sistema.
     *
     * @param body datos necesarios para crear el médico
     * @return CompletableFuture con el médico creado
     */
    public CompletableFuture<MedicoModel> crear(MedicoCreateBody body) {
        return post(body).thenApply(json -> {
            System.out.println(json);
            try{
                return mapper.readValue(json, MedicoModel.class);
            }catch (Exception e){
                throw new RuntimeException("Error parseando respuesta: " + e.getMessage(), e);
            }
        });
    }

    /**
     * Actualiza la información de un médico existente.
     *
     * @param documento documento del médico a actualizar
     * @param body nuevos datos del médico
     * @return CompletableFuture con el médico actualizado
     */
    public CompletableFuture<MedicoModel> actualizar(String documento, MedicoUpdateBody body){
        return put(documento, body).thenApply(json -> {
            try{
                return mapper.readValue(json, MedicoModel.class);
            }catch (Exception e){
                throw new RuntimeException("Error parseando respuesta: " + e.getMessage(), e);
            }
        });
    }

    /**
     * Cambia el estado de un médico entre habilitado e inhabilitado.
     *
     * @param documento documento del médico
     * @return CompletableFuture con el médico actualizado
     */
    public CompletableFuture<MedicoModel> toggleEstado(String documento) {
        return delete(documento)
                .thenApply(json -> {
                    try {
                        if (json == null || json.isBlank()) {
                            return null;
                        }
                        return mapper.readValue(json, MedicoModel.class);

                    } catch (Exception e) {
                        throw new RuntimeException(
                                "Error parseando respuesta: " + e.getMessage(), e);
                    }
                });
    }

    /**
     * Record utilizado para crear médicos.
     * Contiene la información necesaria para registrar un médico.
     */
    public record MedicoCreateBody(
            String documento,
            String nombre,
            String apellido,
            String email,
            String telefono,
            String codigoCiudad,
            int idEspecialidad



    ){}

    /**
     * Record utilizado para actualizar médicos.
     * Contiene la información modificable de un médico.
     */
    public record MedicoUpdateBody(
        String nombre,
        String apellido,
        Integer idEspecialidad,
        String telefono,
        String email,
        String codigoCiudad
    ){}
}