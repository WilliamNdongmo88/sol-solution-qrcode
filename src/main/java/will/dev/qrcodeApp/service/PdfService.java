package will.dev.qrcodeApp.service;

import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.firebase.cloud.StorageClient;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import will.dev.qrcodeApp.config.FirebaseInitializer;
import will.dev.qrcodeApp.entity.PdfMetadata;
import will.dev.qrcodeApp.entity.User;
import will.dev.qrcodeApp.entity.UserAction;
import will.dev.qrcodeApp.repository.PdfMetadataRepository;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Blob;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class PdfService {

    private final PdfMetadataRepository pdfMetadataRepository;
    private final UserActionService userActionService;
    private final FirebaseStorageService firebaseStorageService;
    private final Storage storage;

    @Transactional
    public PdfMetadata uploadPdf(User user, MultipartFile file) throws IOException {
        // Vérification du fichier
        if (file.isEmpty()) {
            throw new IllegalArgumentException("❌ Le fichier est vide.");
        }

        if (!"application/pdf".equals(file.getContentType())) {
            throw new IllegalArgumentException("❌ Seuls les fichiers PDF sont autorisés.");
        }

        // Vérifier si le fichier existe déjà pour cet utilisateur
        if (pdfMetadataRepository.existsByOriginalFilenameAndUser(file.getOriginalFilename(), user)) {
            throw new IllegalArgumentException("❌ Ce fichier existe déjà pour cet utilisateur.");
        }

        // Générer un ID unique
        String uniqueId = UUID.randomUUID().toString();

        // Appeler le service Firebase pour uploader et obtenir l'URL
        String fileUrl = firebaseStorageService.uploadFile(file, uniqueId);
        System.out.println("✅ Fichier uploadé sur Firebase. URL : " + fileUrl);

        // Créer les métadonnées
        PdfMetadata pdfMetadata = new PdfMetadata();
        pdfMetadata.setUniqueId(uniqueId);
        pdfMetadata.setOriginalFilename(file.getOriginalFilename());
        pdfMetadata.setFilePath(fileUrl);
        pdfMetadata.setFileSize(file.getSize());
        pdfMetadata.setUploadDate(LocalDateTime.now());
        pdfMetadata.setContentType(file.getContentType());
        pdfMetadata.setUser(user);

        userActionService.logAction(
                user,
                UserAction.TypeAction.UPLOAD_PDF,
                "Upload du pdf :: " + pdfMetadata.getOriginalFilename()
        );

        // Sauvegarder en BD
        return pdfMetadataRepository.save(pdfMetadata);
    }

    public Optional<PdfMetadata> getPdfMetadata(String uniqueId) {
        return pdfMetadataRepository.findByUniqueId(uniqueId);
    }

    public String extractTextFromPdf(String uniqueId) throws IOException {
        Optional<PdfMetadata> pdfMetadata = getPdfMetadata(uniqueId);
        if (pdfMetadata.isEmpty()) {
            throw new IllegalArgumentException("PDF non trouvé avec l'ID: " + uniqueId);
        }

        String cloudinaryUrl = pdfMetadata.get().getFilePath();
        if (cloudinaryUrl == null || cloudinaryUrl.isBlank()) {
            throw new IOException("Le fichier PDF n'a pas d'URL Cloudinary");
        }

        // On récupère le PDF depuis Cloudinary via InputStream
        try (InputStream inputStream = new URL(cloudinaryUrl).openStream();
             PDDocument document = PDDocument.load(inputStream)) {

            PDFTextStripper pdfStripper = new PDFTextStripper();
            return pdfStripper.getText(document);
        }
    }


    public boolean pdfExists(String uniqueId) {
        return pdfMetadataRepository.existsByUniqueId(uniqueId);
    }

    /**
     * Supprime un PDF du stockage Firebase et de la base de données.
     */
    @Transactional
    public void deletePdf(User user, Long pdfUniqueId) throws IOException {
        // 1️⃣ Récupérer le PDF depuis la base de données
        PdfMetadata pdfMetadata = pdfMetadataRepository.findById(pdfUniqueId)
                .orElseThrow(() -> new IllegalArgumentException("PDF introuvable avec l'ID : " + pdfUniqueId));

        // 3️⃣ Supprimer le fichier du stockage Firebase
        String filePath = pdfMetadata.getFilePath(); // ex: "pdfs/monfichier.pdf"
        firebaseStorageService.deleteFileFromUrl(filePath);

        // 4️⃣ Supprimer les métadonnées en base
        pdfMetadataRepository.delete(pdfMetadata);

        // 5️⃣ Loguer l’action utilisateur
        userActionService.logAction(
                user,
                UserAction.TypeAction.SUPPRESSION_PDF,
                "Suppression du PDF : " + pdfMetadata.getOriginalFilename()
        );
    }


    public List<PdfMetadata> getUserPdfs(User user) {
        return pdfMetadataRepository.findByUserOrderByUploadDateDesc(user);
    }

    public boolean existsByUniqueIdAndUploadedFileName(String uniqueId, String uploadedFileName) {
        return pdfMetadataRepository.existsByUniqueIdAndOriginalFilename(uniqueId, uploadedFileName);
    }
}


