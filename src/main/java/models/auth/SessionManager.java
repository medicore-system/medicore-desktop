package models.auth;

public class SessionManager {
    private static SessionManager instance;
    private String token;
    private String role;

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public void setSession(String token, String role) {
        this.token = token;
        this.role = role;
    }

    public String getToken() { return token; }
    public String getRole() { return role; }

    public void clear() {
        token = null;
        role = null;
    }
}
