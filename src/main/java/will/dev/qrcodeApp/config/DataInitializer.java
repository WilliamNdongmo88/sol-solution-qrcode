package will.dev.qrcodeApp.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import will.dev.qrcodeApp.entity.User;
import will.dev.qrcodeApp.repository.UserRepository;

import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Créer l'administrateur par défaut si n'existe pas
        if (userRepository.findByEmail("williamndongmo899@gmail.com").isEmpty()) {
            User admin = new User();
            admin.setNom("Admin SSAC");
            admin.setEmail("williamndongmo899@gmail.com");
            admin.setPassword(passwordEncoder.encode("Will123"));
            admin.setRole(User.Role.ADMIN);
            admin.setActif(true);
            admin.setDateCreation(LocalDateTime.now());
            userRepository.save(admin);
            System.out.println("Administrateur par défaut créé.");
        }

        // Créer le manager par défaut si n'existe pas
        if (userRepository.findByEmail("ndonwill1@gmail.com").isEmpty()) {
            User manager = new User();
            manager.setNom("Manager SSAC");
            manager.setEmail("ndonwill1@gmail.com");
            manager.setPassword(passwordEncoder.encode("Will123"));
            manager.setRole(User.Role.MANAGER);
            manager.setActif(true);
            manager.setDateCreation(LocalDateTime.now());
            userRepository.save(manager);
            System.out.println("Manager par défaut créé.");
        }

        // Créer l'utilisateur de test par défaut si n'existe pas
        if (userRepository.findByEmail("ndonwill2@gmail.com").isEmpty()) {
            User user = new User();
            user.setNom("User Test");
            user.setEmail("ndonwill2@gmail.com");
            user.setCodeAcces("WILL123"); // Code d'accès pour l'utilisateur
            user.setRole(User.Role.USER);
            user.setActif(true);
            user.setDateCreation(LocalDateTime.now());
            userRepository.save(user);
            System.out.println("Utilisateur de test par défaut créé.");
        }
    }
}


