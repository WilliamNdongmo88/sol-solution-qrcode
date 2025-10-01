package will.dev.qrcodeApp.dto;

public class UpdateProfileRequest {
    private String nom;
    private String email;

    // Constructeurs
    public UpdateProfileRequest() {}

    public UpdateProfileRequest(String nom, String email) {
        this.nom = nom;
        this.email = email;
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
}

