package will.dev.qrcodeApp.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.gson.JsonObject;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Configuration
public class FirebaseConfig {

    @Bean
    public Storage storage() throws IOException {
        // 🔹 Lire les variables d’environnement
        String type = System.getenv("FIREBASE_TYPE");
        String projectId = System.getenv("FIREBASE_PROJECT_ID");
        String privateKeyId = System.getenv("FIREBASE_PRIVATE_KEY_ID");
        String privateKey = System.getenv("FIREBASE_PRIVATE_KEY");
        String clientEmail = System.getenv("FIREBASE_CLIENT_EMAIL");
        String clientId = System.getenv("FIREBASE_CLIENT_ID");

        if (privateKey == null) {
            throw new IllegalStateException("❌ Variable d'environnement FIREBASE_PRIVATE_KEY manquante !");
        }

        // ⚠️ Important : remplacer les "\n" par des vrais sauts de ligne
        privateKey = privateKey.replace("\\n", "\n");
        System.out.println("privateKey :: "+privateKey.substring(0, 50));
        // 🔹 Construire le JSON attendu par GoogleCredentials
        JsonObject json = new JsonObject();
        json.addProperty("type", type);
        json.addProperty("project_id", projectId);
        json.addProperty("private_key_id", privateKeyId);
        json.addProperty("private_key", privateKey);
        json.addProperty("client_email", clientEmail);
        json.addProperty("client_id", clientId);
        json.addProperty("auth_uri", "https://accounts.google.com/o/oauth2/auth");
        json.addProperty("token_uri", "https://oauth2.googleapis.com/token");
        json.addProperty("auth_provider_x509_cert_url", "https://www.googleapis.com/oauth2/v1/certs");
        json.addProperty("client_x509_cert_url", "https://www.googleapis.com/robot/v1/metadata/x509/" + clientEmail);

        // 🔹 Convertir en InputStream
        InputStream credentialsStream =
                new ByteArrayInputStream(json.toString().getBytes(StandardCharsets.UTF_8));

        // 🔹 Construire le bean Storage
        return StorageOptions.newBuilder()
                .setCredentials(GoogleCredentials.fromStream(credentialsStream))
                .setProjectId(projectId)
                .build()
                .getService();
    }
}
