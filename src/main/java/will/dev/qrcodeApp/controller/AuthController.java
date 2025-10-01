package will.dev.qrcodeApp.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import will.dev.qrcodeApp.dto.*;
import will.dev.qrcodeApp.entity.User;
import will.dev.qrcodeApp.repository.UserRepository;
import will.dev.qrcodeApp.service.AuthService;
import will.dev.qrcodeApp.service.JwtService;
import will.dev.qrcodeApp.service.UserDetailsImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final AuthService authService;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register(@RequestBody RegisterRequest request) {
        try{
            String jwtToken = authService.register(request.toUser());
            return ResponseEntity.ok(new LoginResponse(jwtToken, "Inscription réussie"));
        } catch (RuntimeException e) {
            throw new RuntimeException("INSCRIPTION_ERROR:: " + e.getMessage());
        }
    }

    @PostMapping("/authenticate")
    public ResponseEntity<?> authenticate(@RequestBody LoginRequest request) {
        try{
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwtToken = authService.authenticate(authentication);
            User user = (User) authentication.getPrincipal();
            System.out.println("user  :: "+ user);
            System.out.println("user Authorities :: "+ user.getAuthorities());

            List<String> roles = user.getAuthorities().stream()
                    .map(item -> item.getAuthority())
                    .collect(Collectors.toList());

            // Créer le refresh token
//            RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getId());

            return ResponseEntity.ok(new JwtResponse(jwtToken,
                    //refreshToken.getToken(),
                    user.getId(),
                    user.getNom(),
                    user.getEmail(),
                    "Authentification réussie",
                    roles));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("CONNEXION_ERROR:: Nom d'utilisateur ou mot de passe incorrect!"));
        }
    }

    @PostMapping("/authenticate-user-code")
    public ResponseEntity<?> authenticateUserWithCode(@RequestBody LoginRequest request) {
        //String jwtToken = authService.authenticateUserWithCode(request.getEmail(), request.getCodeAcces());
        //return ResponseEntity.ok(new LoginResponse(jwtToken, "Authentification réussie avec code d'accès"));

        Optional<User> userOpt = userRepository.findByEmailAndCodeAcces(request.getEmail(), request.getCodeAcces());
        String jwtToken = authService.authenticateUserWithCode(request.getEmail(), request.getCodeAcces());
        User user = new User();
        List<String> roles = new ArrayList<>();
        //RefreshToken refreshToken = new RefreshToken();
        if (userOpt.isPresent()){
            user = userOpt.get();
            System.out.println("user  :: "+ user);
            System.out.println("user Authorities :: "+ user.getAuthorities());

            roles = user.getAuthorities().stream()
                    .map(item -> item.getAuthority())
                    .collect(Collectors.toList());

            // Créer le refresh token
            // refreshToken = refreshTokenService.createRefreshToken(userDetails.getId());
        }
        return ResponseEntity.ok(new JwtResponse(jwtToken,
                //refreshToken.getToken(),
                user.getId(),
                user.getNom(),
                user.getEmail(),
                "Authentification réussie avec code d'accès",
                roles));
    }

    @PostMapping("/generate-access-code")
    public ResponseEntity<Map<String, String>> generateAccessCode(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            authService.generateAndSendAccessCode(email);
            return ResponseEntity.ok(Map.of("message", "Un nouveau code d'accès a été généré et envoyé à votre email."));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        // Rien à faire côté backend car JWT est stateless
        return ResponseEntity.ok().build();
    }
}


