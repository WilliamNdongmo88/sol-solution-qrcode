package will.dev.qrcodeApp.dto;

import lombok.Data;
import will.dev.qrcodeApp.entity.User;

import java.time.LocalDateTime;

@Data
public class UserDto {
    private Long id;
    private String nom;
    private String email;
    private User.Role role;
    private Boolean actif;
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
}


