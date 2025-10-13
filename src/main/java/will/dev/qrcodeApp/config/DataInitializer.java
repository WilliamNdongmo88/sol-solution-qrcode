package will.dev.qrcodeApp.config;

import com.google.api.client.util.Value;
import com.google.cloud.storage.Bucket;
import com.google.firebase.cloud.StorageClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import will.dev.qrcodeApp.entity.User;
import will.dev.qrcodeApp.mapper.PdfMetadataMapper;
import will.dev.qrcodeApp.repository.UserRepository;
import will.dev.qrcodeApp.service.FirebaseStorageService;
import will.dev.qrcodeApp.service.PdfService;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Blob;
import java.sql.SQLException;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    @Value("${upload.logo.dir}")
    private String uploadLogoDir;

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final FirebaseStorageService firebaseStorageService;

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

        // Initialisation existante
//        System.out.println("Initialisation des données...");
//        syncLogoWithFirebase();
//        System.out.println("Synchronisation Firebase terminée !");
    }

    public void syncLogoWithFirebase() throws IOException {

        FirebaseInitializer.initialize();

        String uploadLogoDir = "C:/Users/DELL/Desktop/Projet/sol-solution2/back-end/uploads/logos";

        File directory = new File(uploadLogoDir);
        System.out.println("directory :: " + directory.getAbsolutePath());

        if (!directory.exists() || !directory.isDirectory()) {
            throw new IllegalArgumentException("❌ Le dossier n’existe pas : " + uploadLogoDir);
        }

        File[] files = directory.listFiles((dir, name) ->
                name.toLowerCase().endsWith(".png") ||
                        name.toLowerCase().endsWith(".jpg") ||
                        name.toLowerCase().endsWith(".jpeg"));
        System.out.println("files :: " + (files != null ? files.length : "null"));

        if (files == null || files.length == 0) {
            System.out.println("⚠️ Aucun fichier trouvé dans " + uploadLogoDir);
            return;
        }

        Bucket bucket = StorageClient.getInstance().bucket();

        for (File file : files) {
            String fileName = file.getName();
            Blob blob = (Blob) bucket.get(fileName);

            if (blob != null) {
                String existingUrl = "https://firebasestorage.googleapis.com/v0/b/" +
                        bucket.getName() + "/o/" + URLEncoder.encode(fileName, StandardCharsets.UTF_8) + "?alt=media";
                System.out.println("✅ Déjà présent sur Firebase : " + existingUrl);
                continue;
            }

            try (FileInputStream inputStream = new FileInputStream(file)) {
                byte[] fileBytes = inputStream.readAllBytes();
                String firebaseUrl = firebaseStorageService.uploadLogo(fileBytes, fileName);
                System.out.println("🚀 Fichier uploadé sur Firebase : " + firebaseUrl);
            } catch (Exception e) {
                System.err.println("❌ Erreur lors de l’upload de " + fileName + " : " + e.getMessage());
            }
        }
    }
}


