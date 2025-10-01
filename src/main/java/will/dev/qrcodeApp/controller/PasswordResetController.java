package will.dev.qrcodeApp.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import will.dev.qrcodeApp.service.PasswordResetService;

import java.util.Map;

@RestController
@RequestMapping("/api/auth/password-reset")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    @PostMapping("/request")
    public ResponseEntity<Map<String, String>> requestPasswordReset(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            passwordResetService.createPasswordResetToken(email);
            return ResponseEntity.ok(Map.of("message", "Un lien de réinitialisation a été envoyé à votre adresse email"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/validate/{token}")
    public ResponseEntity<Map<String, Boolean>> validateToken(@PathVariable String token) {
        boolean isValid = passwordResetService.validateToken(token);
        return ResponseEntity.ok(Map.of("valid", isValid));
    }

    @PostMapping("/reset")
    public ResponseEntity<Map<String, String>> resetPassword(@RequestBody Map<String, String> request) {
        try {
            String token = request.get("token");
            String newPassword = request.get("newPassword");
            
            boolean success = passwordResetService.resetPassword(token, newPassword);
            if (success) {
                return ResponseEntity.ok(Map.of("message", "Mot de passe réinitialisé avec succès"));
            } else {
                return ResponseEntity.badRequest().body(Map.of("error", "Token invalide ou expiré"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}

