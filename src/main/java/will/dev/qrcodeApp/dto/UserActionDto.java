package will.dev.qrcodeApp.dto;

import jakarta.persistence.Column;
import lombok.Data;
import will.dev.qrcodeApp.entity.UserAction;

import java.time.LocalDateTime;

@Data
public class UserActionDto {
    private Long id;
    private Long userId;
    private Long qrcodeId;
    private String uniquePdfId;
    private Boolean isRelatedToQrCode;
    private String userName;
    private UserAction.TypeAction typeAction;
    private String description;
    private LocalDateTime dateAction;
}


