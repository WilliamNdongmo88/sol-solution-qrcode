package will.dev.qrcodeApp.dto;

import java.util.List;

public class JwtResponse {
    private String accessToken;
    private String refreshToken;
    private String type = "Bearer";
    private Long id;
    private String username;
    private String email;
    private String message;
    private List<String> roles;

    public JwtResponse(String accessToken, String refreshToken, Long id, String username,
                       String message, String email, List<String> roles) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.id = id;
        this.username = username;
        this.email = email;
        this.message = message;
        this.roles = roles;
    }

    // Constructeur pour compatibilité avec l'ancien code
    public JwtResponse(String accessToken, Long id, String username, String email, String message, List<String> roles) {
        this.accessToken = accessToken;
        this.id = id;
        this.username = username;
        this.email = email;
        this.message = message;
        this.roles = roles;
    }

    // Getters and Setters
    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getTokenType() {
        return type;
    }

    public void setTokenType(String tokenType) {
        this.type = tokenType;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }
}
