package will.dev.qrcodeApp.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.StorageClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.IOException;

@Component
public class FirebaseInitializer {

    @Value("${firebase.bucket-name}")
    private String bucketName;

    public void initialize() throws IOException {
        // Vérifie si Firebase n'a pas déjà été initialisé
        if (FirebaseApp.getApps().isEmpty()) {
            FileInputStream serviceAccount =
                    new FileInputStream("src/main/resources/firebase/serviceAccountKey.json"); // Chemin vers ton fichier JSON

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setStorageBucket(bucketName) // Remplace par ton bucket
                    .build();

            FirebaseApp.initializeApp(options);
            System.out.println("✅ FirebaseApp initialisé avec succès !");
        }
    }
}

