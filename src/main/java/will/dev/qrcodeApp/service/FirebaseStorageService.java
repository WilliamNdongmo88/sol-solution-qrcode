package will.dev.qrcodeApp.service;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class FirebaseStorageService {

    @Value("${firebase.bucket-name}")
    private String bucketName;

    private final Storage storage;

    public String uploadFile(MultipartFile file, String uniqueId) throws IOException {
        String destinationPath = "pdfs/" + uniqueId + ".pdf";

        storage.create(
                com.google.cloud.storage.BlobInfo.newBuilder(bucketName, destinationPath)
                        .setContentType(file.getContentType())
                        .build(),
                file.getBytes()
        );

        return String.format("https://storage.googleapis.com/%s/%s", bucketName, destinationPath);
    }

    public String uploadLogo(byte[] imageBytes, String name) throws IOException {
        String destinationPath = "logos/" + name;

        BlobId blobId = BlobId.of(bucketName, destinationPath);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType("image/png") // Spécifique pour les images PNG
                .build();

        // Uploader le tableau d'octets directement
        storage.create(blobInfo, imageBytes);

        // Construire l'URL publique
        return String.format("https://storage.googleapis.com/%s/%s", bucketName, destinationPath);
    }

    public String uploadImage(byte[] imageBytes, String uniqueId) throws IOException {
        String destinationPath = "qrcodes/" + uniqueId + ".png";

        BlobId blobId = BlobId.of(bucketName, destinationPath);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType("image/png") // Spécifique pour les images PNG
                .build();

        // Uploader le tableau d'octets directement
        storage.create(blobInfo, imageBytes);

        // Construire l'URL publique
        return String.format("https://storage.googleapis.com/%s/%s", bucketName, destinationPath);
    }

    public boolean fileExists(String folderName, String fileName, String bucketName) {
        try {
            // 🔹 Construire le chemin complet (ex: "logos/Logo-SSAC.png")
            String filePath = folderName != null && !folderName.isEmpty()
                    ? folderName + "/" + fileName
                    : fileName;

            Blob blob = storage.get(bucketName, filePath);
            return blob != null && blob.exists();

        } catch (Exception e) {
            System.err.println("⚠️ Erreur lors de la vérification du fichier : " + e.getMessage());
            return false;
        }
    }

    /**
     * Supprime un fichier de Firebase Storage à partir de son URL publique.
     *
     * @param fileUrl L'URL complète du fichier à supprimer (ex: https://firebasestorage.googleapis.com/... ).
     * @return true si la suppression a réussi, false sinon.
     */
    public boolean deleteFileFromUrl(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return false;
        }

        try {
            String objectPath;

            // Décoder l'URL pour gérer les caractères spéciaux comme '%2F' pour '/'
            String decodedUrl = URLDecoder.decode(fileUrl, StandardCharsets.UTF_8);

            // Cas 1 : URL Firebase avec "?alt=media"
            if (decodedUrl.contains("/o/")) {
                int start = decodedUrl.indexOf("/o/") + 3;
                int end = decodedUrl.indexOf("?alt=media");
                if (end == -1) { // Au cas où "?alt=media" serait manquant
                    end = decodedUrl.length();
                }
                objectPath = decodedUrl.substring(start, end);

                // Cas 2 : URL Google Cloud Storage simple (votre cas)
            } else if (decodedUrl.startsWith("https://storage.googleapis.com/" )) {
                // Le chemin de l'objet est tout ce qui se trouve après le nom du bucket.
                // Exemple: https://storage.googleapis.com/mon-bucket/dossier/fichier.png
                String prefix = "https://storage.googleapis.com/" + bucketName + "/";
                if (!decodedUrl.startsWith(prefix )) {
                    System.err.println("URL GCS invalide, le nom du bucket ne correspond pas : " + decodedUrl);
                    return false;
                }
                objectPath = decodedUrl.substring(prefix.length());

            } else {
                System.err.println("Format d'URL Firebase/GCS non reconnu : " + fileUrl);
                return false;
            }

            System.out.println("Tentative de suppression de l'objet Firebase : '" + objectPath + "'");

            // Vérifier que le chemin n'est pas vide (très important)
            if (objectPath.isEmpty()) {
                System.err.println("Le chemin de l'objet extrait est vide. Suppression annulée.");
                return false;
            }

            BlobId blobId = BlobId.of(bucketName, objectPath);
            return storage.delete(blobId);

        } catch (Exception e) {
            System.err.println("Erreur lors de la suppression du fichier Firebase : " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
