package will.dev.qrcodeApp.mapper;

import org.springframework.stereotype.Component;
import will.dev.qrcodeApp.dto.UserDto;
import will.dev.qrcodeApp.entity.User;

@Component
public class UserMapper {

    public UserDto userToUserDto(User user) {
        if (user == null) {
            return null;
        }

        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setNom(user.getNom());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());   // si c’est une Enum → renvoyée en String par défaut dans le DTO
        dto.setActif(user.getActif());
        dto.setDateCreation(user.getDateCreation());
        dto.setDateModification(user.getDateModification());
        return dto;
    }

    public User userDtoToUser(UserDto dto) {
        if (dto == null) {
            return null;
        }

        User user = new User();
        user.setId(dto.getId());
        user.setNom(dto.getNom());
        user.setEmail(dto.getEmail());
        user.setRole(dto.getRole());
        user.setActif(dto.getActif());
        user.setDateCreation(dto.getDateCreation());
        user.setDateModification(dto.getDateModification());
        // ⚠ on ignore le password volontairement
        return user;
    }
}
