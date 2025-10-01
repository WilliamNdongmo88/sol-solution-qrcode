package will.dev.qrcodeApp.dto;

public class UpdateRoleRequest {
    private String role;

    // Constructeurs
    public UpdateRoleRequest() {}

    public UpdateRoleRequest(String role) {
        this.role = role;
    }

    // Getters et Setters
    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}

