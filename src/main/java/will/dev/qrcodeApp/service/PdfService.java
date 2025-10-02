package will.dev.qrcodeApp.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import will.dev.qrcodeApp.entity.PdfMetadata;
import will.dev.qrcodeApp.entity.User;
import will.dev.qrcodeApp.entity.UserAction;
import will.dev.qrcodeApp.repository.PdfMetadataRepository;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PdfService {

    @Value("${cloudinary.cloud-name}")
    private String cloud_name;

    @Value("${cloudinary.api-key}")
    private String api_key;

    @Value("${cloudinary.api-secret}")
    private String api_secret;

    private final PdfMetadataRepository pdfMetadataRepository;
    private final UserActionService userActionService;
    private final Cloudinary cloudinary;

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

        // Upload sur Cloudinary
        Map uploadResult = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap(
                        "public_id", "pdfs/" + uniqueId,
                        "resource_type", "auto"
                )
        );

        // Créer les métadonnées
        PdfMetadata pdfMetadata = new PdfMetadata();
        pdfMetadata.setUniqueId(uniqueId);
        pdfMetadata.setOriginalFilename(file.getOriginalFilename());
        pdfMetadata.setFilePath(uploadResult.get("secure_url").toString());
        pdfMetadata.setFileSize(file.getSize());
        pdfMetadata.setUploadDate(LocalDateTime.now());
        pdfMetadata.setContentType(file.getContentType());
        pdfMetadata.setUser(user);

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

    @Transactional
    public void deletePdf(User user, String uniqueId) throws IOException {
        Optional<PdfMetadata> pdfMetadataOpt = pdfMetadataRepository.findByUniqueId(uniqueId);
        if (pdfMetadataOpt.isEmpty()) {
            throw new IllegalArgumentException("PDF non trouvé avec l'ID: " + uniqueId);
        }

        PdfMetadata pdfMetadata = pdfMetadataOpt.get();

        // Vérifier les droits
        if (user.getRole() == User.Role.USER && !pdfMetadata.getUser().getId().equals(user.getId())) {
            throw new SecurityException("Vous n'êtes pas autorisé à supprimer ce PDF.");
        }

        // Suppression via Cloudinary
        Cloudinary cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloud_name,
                "api_key", api_key,
                "api_secret", api_secret
        ));

        // Le public_id est celui qu'on avait utilisé à l'upload : "pdfs/" + uniqueId
        String publicId = "pdfs/" + uniqueId;
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("resource_type", "raw"));
        } catch (Exception e) {
            throw new IOException("Erreur lors de la suppression sur Cloudinary : " + e.getMessage());
        }

        // Suppression des métadonnées
        pdfMetadataRepository.delete(pdfMetadata);

        // Log action
        userActionService.logAction(user, UserAction.TypeAction.SUPPRESSION_PDF,
                "PDF " + uniqueId + " supprimé par " + user.getEmail());
    }


    public List<PdfMetadata> getUserPdfs(User user) {
        return pdfMetadataRepository.findByUserOrderByUploadDateDesc(user);
    }

    public boolean existsByUniqueIdAndUploadedFileName(String uniqueId, String uploadedFileName) {
        return pdfMetadataRepository.existsByUniqueIdAndOriginalFilename(uniqueId, uploadedFileName);
    }
}


