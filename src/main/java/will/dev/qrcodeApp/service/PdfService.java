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

    public File getPdfFile(String uniqueId) throws IOException {
        Optional<PdfMetadata> pdfMetadata = getPdfMetadata(uniqueId);
        if (pdfMetadata.isEmpty()) {
            throw new IllegalArgumentException("PDF non trouvé avec l'ID: " + uniqueId);
        }

        File file = new File(pdfMetadata.get().getFilePath());
        if (!file.exists()) {
            throw new IOException("Le fichier PDF n'existe pas sur le disque");
        }

        return file;
    }

    public String extractTextFromPdf(String uniqueId) throws IOException {
        File pdfFile = getPdfFile(uniqueId);

        try (PDDocument document = PDDocument.load(pdfFile)) {
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
        if (pdfMetadataOpt.isPresent()) {
            PdfMetadata pdfMetadata = pdfMetadataOpt.get();

            // Vérifier si l'utilisateur a le droit de supprimer ce PDF (admin/manager ou propriétaire)
            if (user.getRole() == User.Role.USER && !pdfMetadata.getUser().getId().equals(user.getId())) {
                throw new SecurityException("Vous n'êtes pas autorisé à supprimer ce PDF.");
            }

            // Suppression du fichier physique
            File file = new File(pdfMetadata.getFilePath());
            if (file.exists()) {
                Files.delete(file.toPath());
            }

            // Suppression des métadonnées
            pdfMetadataRepository.delete(pdfMetadata);

            // Enregistrer l'action de suppression
            userActionService.logAction(user, UserAction.TypeAction.SUPPRESSION_PDF,
                    "PDF " + uniqueId + " supprimé par " + user.getEmail());
        } else {
            throw new IllegalArgumentException("PDF non trouvé avec l'ID: " + uniqueId);
        }
    }

    public List<PdfMetadata> getUserPdfs(User user) {
        return pdfMetadataRepository.findByUserOrderByUploadDateDesc(user);
    }

    public boolean existsByUniqueIdAndUploadedFileName(String uniqueId, String uploadedFileName) {
        return pdfMetadataRepository.existsByUniqueIdAndOriginalFilename(uniqueId, uploadedFileName);
    }
}


