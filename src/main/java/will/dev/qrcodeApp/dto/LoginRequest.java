package will.dev.qrcodeApp.dto;

public class LoginRequest {
    private String email;
    private String password;
    private String codeAcces;

    // Constructeurs
    public LoginRequest() {}

    public LoginRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public LoginRequest(String email, String password, String codeAcces) {
        this.email = email;
        this.password = password;
        this.codeAcces = codeAcces;
    }

    // Getters et Setters
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getCodeAcces() {
        return codeAcces;
    }

    public void setCodeAcces(String codeAcces) {
        this.codeAcces = codeAcces;
    }
}

