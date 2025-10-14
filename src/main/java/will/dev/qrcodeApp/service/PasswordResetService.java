package will.dev.qrcodeApp.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import will.dev.qrcodeApp.dto.PasswordResetRequest;
import will.dev.qrcodeApp.entity.PasswordResetToken;
import will.dev.qrcodeApp.entity.User;
import will.dev.qrcodeApp.repository.PasswordResetTokenRepository;
import will.dev.qrcodeApp.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final UserRepository userRepository;
    private final BrevoService brevoService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public String createPasswordResetToken(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("Utilisateur non trouvé avec cet email");
        }

        User user = userOpt.get();
        
        // Vérifier que l'utilisateur est un admin
        if (user.getRole() != User.Role.ADMIN) {
            throw new RuntimeException("Seuls les administrateurs peuvent réinitialiser leur mot de passe");
        }

        // Supprimer les anciens tokens non utilisés
        passwordResetTokenRepository.deleteByUser(user);

        // Créer un nouveau token
        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken(token, user);
        passwordResetTokenRepository.save(resetToken);

        // Envoyer l'email de réinitialisation
        brevoService.sendPasswordResetEmail(user.getEmail(), user.getNom(), token);

        return token;
    }

    @Transactional
    public boolean resetPassword(String token, String newPassword) {
        Optional<PasswordResetToken> tokenOpt = passwordResetTokenRepository
                .findValidToken(token, LocalDateTime.now());

        if (tokenOpt.isEmpty()) {
            return false; // Token invalide ou expiré
        }

        PasswordResetToken resetToken = tokenOpt.get();
        User user = resetToken.getUser();

        // Mettre à jour le mot de passe
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Marquer le token comme utilisé
        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);

        return true;
    }

    public boolean validateToken(String token) {
        Optional<PasswordResetToken> tokenOpt = passwordResetTokenRepository
                .findValidToken(token, LocalDateTime.now());
        return tokenOpt.isPresent();
    }

    @Transactional
    public void cleanupExpiredTokens() {
        passwordResetTokenRepository.deleteExpiredTokens(LocalDateTime.now());
    }

    public void changePassword(User user, PasswordResetRequest request) {
        // Vérification du mot de passe actuel
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Mot de passe actuel incorrect.");
        }

        // Vérification de la confirmation
        if (!request.getNewPassword().equals(request.getNewPasswordConfirm())) {
            throw new IllegalArgumentException("Les nouveaux mots de passe ne correspondent pas.");
        }

        // Vérification de la complexité (exemple)
//        if (request.getNewPassword().length() < 8) {
//            throw new IllegalArgumentException("⚠️ Le nouveau mot de passe doit contenir au moins 8 caractères.");
//        }

        // Mise à jour du mot de passe
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }
}

