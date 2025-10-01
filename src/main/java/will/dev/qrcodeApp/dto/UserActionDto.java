package will.dev.qrcodeApp.dto;

import lombok.Data;
import will.dev.qrcodeApp.entity.UserAction;

import java.time.LocalDateTime;

@Data
public class UserActionDto {
    private Long id;
    private Long userId;
    private String userName;
    private UserAction.TypeAction typeAction;
    private String description;
    private LocalDateTime dateAction;
}


