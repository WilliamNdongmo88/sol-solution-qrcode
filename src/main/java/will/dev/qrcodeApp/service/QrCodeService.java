package will.dev.qrcodeApp.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import will.dev.qrcodeApp.entity.PdfMetadata;
import will.dev.qrcodeApp.entity.QrCodeMetadata;
import will.dev.qrcodeApp.entity.User;
import will.dev.qrcodeApp.entity.UserAction;
import will.dev.qrcodeApp.repository.PdfMetadataRepository;
import will.dev.qrcodeApp.repository.QrCodeMetadataRepository;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QrCodeService {

    private final QrCodeMetadataRepository qrCodeMetadataRepository;
    private final PdfMetadataRepository pdfMetadataRepository;
    private final UserActionService userActionService;
    private final EmailService emailService;

    @Value("${app.qrcode.dir:uploads/qrcodes}")
    private String qrCodeDir;

    @Value("${app.base.url}")
    private String baseUrl;

    @Transactional
    public QrCodeMetadata generateQrCode(String pdfUniqueId, String logoPath) throws IOException, WriterException {
        // Vérification que le PDF existe
        PdfMetadata pdfMetadata = pdfMetadataRepository.findByUniqueId(pdfUniqueId)
                .orElseThrow(() -> new IllegalArgumentException("PDF non trouvé avec l'ID unique: " + pdfUniqueId));

        // Vérification si un QR code existe déjà pour ce PDF et cet utilisateur
        Optional<QrCodeMetadata> existingQrCode = qrCodeMetadataRepository.findByPdfMetadataAndUser(pdfMetadata, pdfMetadata.getUser());
        if (existingQrCode.isPresent()) {
            userActionService.logAction(pdfMetadata.getUser(), UserAction.TypeAction.GENERATION_QR, "QR Code déjà existant pour le PDF " + pdfUniqueId + ". Retour de l'existant.");
            return existingQrCode.get();
        }

        // Génération d'un ID unique pour le QR code
        String qrCodeUniqueId = UUID.randomUUID().toString();

        // URL qui sera encodée dans le QR code (pointe vers l'endpoint de visualisation du PDF)
        String qrContent = baseUrl + "/api/pdf/view/" + pdfUniqueId;

        // Création du répertoire de QR codes s'il n'existe pas
        Path qrCodePath = Paths.get(qrCodeDir);
        if (!Files.exists(qrCodePath)) {
            Files.createDirectories(qrCodePath);
        }

        // Génération de l'image QR code
        String filename = qrCodeUniqueId + ".png";
        Path filePath = qrCodePath.resolve(filename);
        generateQrCodeImage(qrContent, filePath.toString(), 300, 300, logoPath);

        // Création des métadonnées
        List<QrCodeMetadata> existingQrCodeList = qrCodeMetadataRepository.findAll();
        QrCodeMetadata qrCodeMetadata = new QrCodeMetadata();
        qrCodeMetadata.setUniqueId(qrCodeUniqueId);
        qrCodeMetadata.setQrName("QR00"+(existingQrCodeList.size()+1));
        qrCodeMetadata.setFilePath(filePath.toString());
        qrCodeMetadata.setPdfId(pdfUniqueId);
        qrCodeMetadata.setQrContent(qrContent);
        qrCodeMetadata.setGenerationDate(LocalDateTime.now());
        qrCodeMetadata.setImageFormat("PNG");
        qrCodeMetadata.setImageSize(300); // Taille par défaut
        qrCodeMetadata.setUser(pdfMetadata.getUser());
        qrCodeMetadata.setPdfMetadata(pdfMetadata);

        QrCodeMetadata savedQrCode = qrCodeMetadataRepository.save(qrCodeMetadata);

        // Enregistrer l'action de génération
        userActionService.logAction(pdfMetadata.getUser(), UserAction.TypeAction.GENERATION_QR, "QR Code généré pour le PDF " + savedQrCode.getPdfMetadata().getOriginalFilename());

        // Envoyer le QR code par email
        emailService.sendQrCodeEmail(pdfMetadata.getUser(), filePath.toString(), qrContent);

        return savedQrCode;
    }

    private void generateQrCodeImage(String text, String filePath, int width, int height, String logoPath) throws WriterException, IOException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();

        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H); // Utiliser H pour une meilleure correction d'erreur avec logo
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");

        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height, hints);

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        image.createGraphics();

        Graphics2D graphics = (Graphics2D) image.getGraphics();
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, width, height);
        graphics.setColor(Color.BLACK); // Couleur du QR code

        for (int i = 0; i < bitMatrix.getWidth(); i++) {
            for (int j = 0; j < bitMatrix.getHeight(); j++) {
                if (bitMatrix.get(i, j)) {
                    graphics.fillRect(i, j, 1, 1);
                }
            }
        }

        if (logoPath != null && !logoPath.isEmpty()) {
            File logoFile = new File(logoPath);
            if (logoFile.exists()) {
                BufferedImage logoImage = ImageIO.read(logoFile);
                if (logoImage != null) {
                    int logoWidth = width / 4; // Taille du logo (ex: 1/4 de la taille du QR code)
                    int logoHeight = height / 4;
                    int x = (width - logoWidth) / 2;
                    int y = (height - logoHeight) / 2;

                    graphics.drawImage(logoImage, x, y, logoWidth, logoHeight, null);
                }
            }
        }

        ImageIO.write(image, "PNG", new File(filePath));
    }

    public Optional<QrCodeMetadata> getQrCodeMetadata(String uniqueId) {
        return qrCodeMetadataRepository.findByUniqueId(uniqueId);
    }

    public File getQrCodeFile(String uniqueId) throws IOException {
        Optional<QrCodeMetadata> qrCodeMetadata = getQrCodeMetadata(uniqueId);
        if (qrCodeMetadata.isEmpty()) {
            throw new IllegalArgumentException("QR Code non trouvé avec l'ID: " + uniqueId);
        }

        File file = new File(qrCodeMetadata.get().getFilePath());
        if (!file.exists()) {
            throw new IOException("Le fichier QR Code n'existe pas sur le disque");
        }

        return file;
    }

    public boolean qrCodeExists(String uniqueId) {
        return qrCodeMetadataRepository.existsByUniqueId(uniqueId);
    }

    @Transactional
    public void deleteQrCode(String uniqueId) throws IOException {
        Optional<QrCodeMetadata> qrCodeMetadataOpt = qrCodeMetadataRepository.findByUniqueId(uniqueId);
        if (qrCodeMetadataOpt.isPresent()) {
            QrCodeMetadata qrCodeMetadata = qrCodeMetadataOpt.get();

            // Vérifier si l'utilisateur a le droit de supprimer ce QR code (admin/manager ou propriétaire)
            if (qrCodeMetadata.getUser().getRole() == User.Role.USER && !qrCodeMetadata.getUser().getId().equals(qrCodeMetadata.getUser().getId())) {
                throw new SecurityException("Vous n'êtes pas autorisé à supprimer ce QR code.");
            }

            // Suppression du fichier physique
            File file = new File(qrCodeMetadata.getFilePath());
            if (file.exists()) {
                Files.delete(file.toPath());
            }

            // Suppression des métadonnées
            qrCodeMetadataRepository.delete(qrCodeMetadata);

            // Enregistrer l'action de suppression
            userActionService.logAction(qrCodeMetadata.getUser(), UserAction.TypeAction.SUPPRESSION_QR,
                    "QR Code " + uniqueId + " supprimé par " + qrCodeMetadata.getUser().getEmail());
        } else {
            throw new IllegalArgumentException("QR Code non trouvé avec l'ID: " + uniqueId);
        }
    }
}


