package will.dev.qrcodeApp.dto;

import lombok.Data;

@Data
public class CreateUserRequest {
    private String nom;
    private String email;
    private String role;
    private String password;

    // Constructeurs
    public CreateUserRequest() {}

    public CreateUserRequest(String nom, String email, String role) {
        this.nom = nom;
        this.email = email;
        this.role = role;
    }

    // Getters et Setters
    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}

