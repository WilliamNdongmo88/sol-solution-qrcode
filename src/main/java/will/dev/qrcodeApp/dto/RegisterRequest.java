package will.dev.qrcodeApp.dto;

import lombok.Data;
import will.dev.qrcodeApp.entity.User;

@Data
public class RegisterRequest {
    private String nom;
    private String email;
    private String password;
    private User.Role role;

    public User toUser() {
        User user = new User();
        user.setNom(this.nom);
        user.setEmail(this.email);
        user.setPassword(this.password);
        user.setRole(this.role != null ? this.role : User.Role.USER);
        return user;
    }
}


