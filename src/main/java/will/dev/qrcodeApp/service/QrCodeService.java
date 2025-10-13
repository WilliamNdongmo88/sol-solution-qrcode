package will.dev.qrcodeApp.service;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.firebase.cloud.StorageClient;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class QrCodeService {

    private final QrCodeMetadataRepository qrCodeMetadataRepository;
    private final PdfMetadataRepository pdfMetadataRepository;
    private final UserActionService userActionService;
    private final EmailService emailService;
    private final BrevoService brevoService;
    private final FirebaseStorageService firebaseStorageService;
    private final Storage storage;

    @Value("${app.qrcode.dir:uploads/qrcodes}")
    private String qrCodeDir;

    @Value("${app.base.url}")
    private String baseUrl;

    @Value("${firebase.bucket-name}")
    private String bucketName;

    @Transactional
    public QrCodeMetadata generateQrCode(String pdfUniqueId) throws Exception {
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
        String logoFileName = "Logo-SSAC.jpg"; // Le nom du fichier dans firebase storage
        byte[] qrCodeBytes = generateQrCodeImageBytes(qrContent, 300, 300, logoFileName);

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
        try {
            byte[] qrBytes = getQrCodeBytesFromFirebase(qrCodeFirebaseUrl);
            brevoService.sendQrCodeEmail(pdfMetadata.getUser(), qrContent, qrBytes);
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de l'email Brevo: {}", e.getMessage());
        }

        return savedQrCode;
    }

    public byte[] generateQrCodeImageBytes(String text, int width, int height, String logoFileName) throws WriterException, IOException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();

        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");

        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height, hints);

        // ✅ Image avec transparence (ARGB)
        BufferedImage qrImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = qrImage.createGraphics();

        // Active l’antialiasing et la qualité du rendu
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        // Fond blanc transparent
        g.setColor(new Color(255, 255, 255, 255));
        g.fillRect(0, 0, width, height);

        // 🔸 Couleur du QR code (gris foncé au lieu de noir)
        Color qrColor = new Color(30, 30, 30); // gris doux
        g.setColor(qrColor);

        // Dessin du QR code
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (bitMatrix.get(x, y)) {
                    g.fillRect(x, y, 1, 1);
                }
            }
        }

        // Vérification si le nom du fichier sélectionné a l'initialisation de l'application
        // est identique a celui dans firebase storage
        Boolean existingFileName = checkFile(logoFileName);
        if (existingFileName) {
            System.out.println("✅ Logo Déjà présent sur Firebase ! ");
        }else {
            System.out.println("✅ Le logo est absent sur Firebase ! ");
            throw new RuntimeException("ERROR From [QrCodeService.generateQrCodeImageBytes] :" +
                    " Vérifier la presence du fichier dans Firebase Storage et si le nom du fichier" +
                    "correspond bien a celui indiqué a la ligne 79 (String logoFileName = \"Logo-SSAC.jpg\";)" +
                    "de cette même classe [QrCodeService]");
        }

        // 🔹 Ajout du logo sans perte de couleurs
        System.out.println("logoFileName :: "+ logoFileName);
        if (logoFileName != null && !logoFileName.isEmpty()) {
            try {
                BufferedImage logo = getLogoFromFirebase(logoFileName);
                if (logo != null) {
                    int logoWidth = width / 4;
                    int logoHeight = height / 4;
                    int x = (width - logoWidth) / 2;
                    int y = (height - logoHeight) / 2;

                    g.setComposite(AlphaComposite.SrcOver);
                    g.drawImage(logo, x, y, logoWidth, logoHeight, null);
                }
            } catch (IOException e) {
                System.out.println("⚠️ Impossible de récupérer le logo depuis Firebase : " + e.getMessage());
            }
        }

        // ✅ Bordures arrondies
        int cornerRadius = 40; // rayon d'arrondi des coins
        BufferedImage roundedQr = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = roundedQr.createGraphics();

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Masque arrondi
        g2.setClip(new RoundRectangle2D.Double(1, 1, width, height, cornerRadius, cornerRadius));
        g2.drawImage(qrImage, 1, 1, null);
        g2.dispose();
        g.dispose();

        // 🔸 Conversion finale en byte[]
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(roundedQr, "PNG", baos);
            return baos.toByteArray();
        }
    }

    private BufferedImage getLogoFromFirebase(String logoFileName) throws IOException {
        Bucket bucket = storage.get(bucketName);
        if (bucket == null) {
            throw new IOException("❌ Bucket introuvable : " + bucketName);
        }
        String fullPath = "logos/" + logoFileName;
        Blob blob = bucket.get(fullPath);

        if (blob == null) {
            throw new IOException("❌ Le fichier " + fullPath + " n'existe pas dans Firebase Storage.");
        }

        byte[] bytes = blob.getContent();
        return ImageIO.read(new ByteArrayInputStream(bytes));
    }

    public byte[] getQrCodeBytesFromFirebase(String qrCodeUrl) throws Exception {
        URL url = new URL(qrCodeUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.connect();

        if (connection.getResponseCode() != 200) {
            throw new IllegalArgumentException("Impossible de récupérer le fichier depuis Firebase : " + connection.getResponseCode());
        }

        try (InputStream inputStream = connection.getInputStream();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            return outputStream.toByteArray();
        }
    }

    public Boolean checkFile(String fileName) {
        String bucketName = "solsolutionpdf.firebasestorage.app";
        String folder = "logos";

        boolean exists = firebaseStorageService.fileExists(folder, fileName, bucketName);

        if (exists) {
            return true;
        } else {
            return false;
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


