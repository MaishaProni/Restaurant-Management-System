package com.restaurant.client.util;

/**
 * Singleton Pattern — holds the authenticated user's session data
 * so all controllers can access the JWT token and user info.
 */
public class SessionManager {

    private static SessionManager instance;

    private String token;
    private String username;
    private String fullName;
    private String role;
    private Integer userId;

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public void setSession(String token, String username, String fullName, String role, Integer userId) {
        this.token    = token;
        this.username = username;
        this.fullName = fullName;
        this.role     = role;
        this.userId   = userId;
    }

    public void clearSession() {
        this.token    = null;
        this.username = null;
        this.fullName = null;
        this.role     = null;
        this.userId   = null;
    }

    public String getToken()    { return token; }
    public String getUsername() { return username; }
    public String getFullName() { return fullName; }
    public String getRole()     { return role; }
    public Integer getUserId()  { return userId; }
    public boolean isLoggedIn() { return token != null; }
}
