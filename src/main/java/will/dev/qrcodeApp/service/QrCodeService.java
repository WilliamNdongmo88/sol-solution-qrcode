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
import java.io.ByteArrayOutputStream;
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
    private final FirebaseStorageService firebaseStorageService;
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

        // 1. Génération de l'ID unique et du contenu du QR code
        String qrCodeUniqueId = UUID.randomUUID().toString();
        String qrContent = baseUrl + "/api/pdf/view/" + pdfUniqueId;

        // 2. Génération de l'image QR code EN MÉMOIRE
        byte[] qrCodeBytes = generateQrCodeImageBytes(qrContent, 300, 300, logoPath);

        // 3. Upload de l'image sur Firebase Storage
        String qrCodeFirebaseUrl = firebaseStorageService.uploadImage(qrCodeBytes, qrCodeUniqueId);
        System.out.println("✅ QR Code uploadé sur Firebase. URL : " + qrCodeFirebaseUrl);

        // Création des métadonnées
        List<QrCodeMetadata> existingQrCodeList = qrCodeMetadataRepository.findAll();
        QrCodeMetadata qrCodeMetadata = new QrCodeMetadata();
        qrCodeMetadata.setUniqueId(qrCodeUniqueId);
        qrCodeMetadata.setQrName("QR00"+(existingQrCodeList.size()+1));
        qrCodeMetadata.setFilePath(qrCodeFirebaseUrl);
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
        emailService.sendQrCodeEmail(pdfMetadata.getUser(), qrCodeFirebaseUrl, qrContent);

        return savedQrCode;
    }

    private byte[] generateQrCodeImageBytes(String text, int width, int height, String logoPath) throws WriterException, IOException {
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

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            // Écrit l'image finale au format PNG dans le flux en mémoire
            ImageIO.write(image, "PNG", baos);

            // Retourne le contenu du flux sous forme de tableau d'octets
            return baos.toByteArray();
        }
    }

    public Optional<QrCodeMetadata> getQrCodeMetadata(String uniqueId) {
        return qrCodeMetadataRepository.findByUniqueId(uniqueId);
    }

    public boolean qrCodeExists(String uniqueId) {
        return qrCodeMetadataRepository.existsByUniqueId(uniqueId);
    }

    @Transactional
    public void deleteQrCode(User user, Long qrCodeId) {
        // 1. Récupérer les métadonnées pour obtenir l'URL du fichier
        QrCodeMetadata qrCode = qrCodeMetadataRepository.findById(qrCodeId)
                .orElseThrow(() -> new IllegalArgumentException("QR Code non trouvé avec l'ID: " + qrCodeId));

        // 2. Tenter de supprimer le fichier sur Firebase Storage
        boolean deletedFromFirebase = firebaseStorageService.deleteFileFromUrl(qrCode.getFilePath());

        if (!deletedFromFirebase) {
            throw new RuntimeException("Impossible de supprimer le fichier Firebase : " + qrCode.getFilePath());
        }

        // 3. Si la suppression sur Firebase a réussi, supprimer l'entrée de la base de données
        qrCodeMetadataRepository.delete(qrCode);

        userActionService.logAction(
                user,
                UserAction.TypeAction.SUPPRESSION_QR,
                "Suppression du QR Code : " + qrCode.getQrName()
        );

        System.out.println("QR Code " + qrCodeId + " et fichier associé supprimés avec succès.");
    }

}


