package will.dev.qrcodeApp.dto;

import lombok.Data;

@Data
public class PasswordResetRequest {
    private String currentPassword;
    private String newPassword;
    private String newPasswordConfirm;
}
