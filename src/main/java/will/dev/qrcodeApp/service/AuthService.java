package will.dev.qrcodeApp.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import will.dev.qrcodeApp.entity.User;
import will.dev.qrcodeApp.repository.UserRepository;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailService emailService;

    public String register(User request) {
        User user = new User();
        user.setNom(request.getNom());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(User.Role.USER);
        user.setActif(true);
        userRepository.save(user);
        return jwtService.generateToken(user);
    }

    public String authenticate(Authentication authentication) {
//        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();
        User user = (User) authentication.getPrincipal();
        return jwtService.generateToken(user);
    }

    public String authenticateUserWithCode(String email, String codeAcces) {
        System.out.println("email::" + email);
        System.out.println("codeAcces::" + codeAcces);
        Optional<User> userOpt = userRepository.findByEmailAndCodeAcces(email, codeAcces);
        System.out.println("userOpt::" + userOpt.get());
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (user.getActif() && user.getRole() == User.Role.USER) {
                return jwtService.generateToken(user);
            }
        }
        throw new RuntimeException("Email ou code d'accès invalide");
    }

    public void generateAndSendAccessCode(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("Utilisateur non trouvé");
        }
        User user = userOpt.get();
        if (user.getRole() != User.Role.USER) {
            throw new RuntimeException("Cette fonction est réservée aux utilisateurs standards");
        }

        String code = UUID.randomUUID().toString().substring(0, 8).toUpperCase(); // Générer un code court
        user.setCodeAcces(code);
        userRepository.save(user);
        emailService.sendWelcomeEmail(user, code); // Réutiliser le template de bienvenue pour envoyer le code
    }
}


