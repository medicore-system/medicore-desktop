package services;

import models.auth.AuthDetails;
import services.http.HttpServiceImpl;

import java.util.concurrent.CompletableFuture;

public class AuthService extends HttpServiceImpl<Object, String> {
    private static final String BASE_URL = dotenv.get("URL_API_AUTH","http://localhost:8080/auth/login");
    private static AuthService instance;

    private AuthService() {
        super(BASE_URL);
    }

    public static AuthService getInstance() {
        if (instance == null) instance = new AuthService();
        return instance;
    }

    public CompletableFuture<AuthDetails> login(AuthCreateBody body){
        return post(body).thenApply(json -> {
            try{
                return mapper.readValue(json, AuthDetails.class);
            }catch (Exception e){
                throw new RuntimeException("error parseando respuesta: " + e.getMessage(), e);
            }
        });
    }

    /**
     * Cuerpo de la petición POST /login.
     * Mapea exactamente LoginRequestDTO del backend.
     */
    public record AuthCreateBody(
            String correo,
            String contrasena
    ){}
}
