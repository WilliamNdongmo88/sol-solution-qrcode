package will.dev.qrcodeApp.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class JwtResponse {
    // Getters and Setters
    private String accessToken;
    private String refreshToken;
    private String type = "Bearer";
    private Long id;
    private String nom;
    private String email;
    private String message;
    private List<String> roles;

    public JwtResponse(String accessToken, String refreshToken, Long id, String nom,
                       String message, String email, List<String> roles) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.id = id;
        this.nom = nom;
        this.email = email;
        this.message = message;
        this.roles = roles;
    }
}
