package will.dev.qrcodeApp.controller;

import com.google.zxing.WriterException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.client.RestTemplate;
import will.dev.qrcodeApp.dto.QrCodeGenerationResponse;
import will.dev.qrcodeApp.entity.QrCodeMetadata;
import will.dev.qrcodeApp.entity.User;
import will.dev.qrcodeApp.service.QrCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api/qrcode")
@RequiredArgsConstructor
public class QrCodeController {

    private final QrCodeService qrCodeService;

    @Value("${BASE_URL}")
    private String baseUrl;

    @PostMapping("/generate/{pdfId}")
    public ResponseEntity<?> generateQrCode(@PathVariable String pdfId,
                                            @RequestParam(required = false) MultipartFile logo) {
        try {
            String logoPath = null;

            Path logoDir = Paths.get("uploads/logos");
            if (!Files.exists(logoDir)) {
                Files.createDirectories(logoDir);
            }

            // 🔎 Vérifier si un logo existe déjà dans le dossier
            try (Stream<Path> files = Files.list(logoDir)) {
                Optional<Path> existingLogo = files
                        .filter(Files::isRegularFile) // garder uniquement les fichiers
                        .findFirst(); // prendre le premier trouvé

                if (existingLogo.isPresent()) {
                    logoPath = existingLogo.get().toString();
                    System.out.println("✔ Logo déjà présent utilisé : " + logoPath);
                } else if (logo != null && !logo.isEmpty()) {
                    // Sinon, si aucun logo présent, on enregistre le nouveau
                    String logoFileName = UUID.randomUUID().toString() + "_" + logo.getOriginalFilename();
                    Path logoFilePath = logoDir.resolve(logoFileName);
                    Files.copy(logo.getInputStream(), logoFilePath);
                    logoPath = logoFilePath.toString();
                    System.out.println("📥 Nouveau logo sauvegardé : " + logoPath);
                }
            }

            QrCodeMetadata qrCodeMetadata = qrCodeService.generateQrCode(pdfId, logoPath);

            String downloadUrl = baseUrl + "/api/qrcode/download/" + qrCodeMetadata.getUniqueId();

            QrCodeGenerationResponse response = new QrCodeGenerationResponse(
                    qrCodeMetadata.getUniqueId(),
                    qrCodeMetadata.getPdfId(),
                    qrCodeMetadata.getQrContent(),
                    downloadUrl,
                    qrCodeMetadata.getGenerationDate(),
                    "QR Code généré avec succès"
            );

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Erreur: " + e.getMessage());
        } catch (IOException | WriterException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de la génération du QR Code: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @GetMapping("/download/{qrCodeId}")
    public ResponseEntity<byte[]> downloadQrCode(@PathVariable String qrCodeId) {
        Optional<QrCodeMetadata> qrCodeMetadata = qrCodeService.getQrCodeMetadata(qrCodeId);

        if (qrCodeMetadata.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        String fileUrl = qrCodeMetadata.get().getFilePath();

        // Télécharge le fichier depuis Firebase Storage
        RestTemplate restTemplate = new RestTemplate();
        byte[] fileBytes = restTemplate.getForObject(fileUrl, byte[].class);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);
        headers.setContentDispositionFormData("attachment", "qrcode.png");

        return new ResponseEntity<>(fileBytes, headers, HttpStatus.OK);
    }


    @GetMapping("/view/{qrCodeId}" )
    public ResponseEntity<Void> viewQrCode(@PathVariable String qrCodeId) {
        // 1. Récupérer les métadonnées du QR code depuis la base de données
        Optional<QrCodeMetadata> qrCodeMetadataOpt = qrCodeService.getQrCodeMetadata(qrCodeId);

        if (qrCodeMetadataOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // 2. Obtenir l'URL publique Firebase stockée dans le champ filePath
        String firebaseQrCodeUrl = qrCodeMetadataOpt.get().getFilePath();

        // 3. Rediriger le navigateur de l'utilisateur vers cette URL
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(firebaseQrCodeUrl))
                .build();
    }

    @GetMapping("/info/{qrCodeId}")
    public ResponseEntity<?> getQrCodeInfo(@PathVariable String qrCodeId) {
        Optional<QrCodeMetadata> qrCodeMetadata = qrCodeService.getQrCodeMetadata(qrCodeId);

        if (qrCodeMetadata.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(qrCodeMetadata.get());
    }
}

