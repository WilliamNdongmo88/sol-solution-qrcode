package will.dev.qrcodeApp.controller;

import com.google.zxing.WriterException;
import will.dev.qrcodeApp.dto.QrCodeGenerationResponse;
import will.dev.qrcodeApp.entity.QrCodeMetadata;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api/qrcode")
public class QrCodeController {

    @Autowired
    private QrCodeService qrCodeService;

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
        }
    }


    @GetMapping("/download/{qrCodeId}")
    public ResponseEntity<Resource> downloadQrCode(@PathVariable String qrCodeId) {
        try {
            File qrCodeFile = qrCodeService.getQrCodeFile(qrCodeId);
            Optional<QrCodeMetadata> qrCodeMetadata = qrCodeService.getQrCodeMetadata(qrCodeId);
            
            if (qrCodeMetadata.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            Resource resource = new FileSystemResource(qrCodeFile);
            
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                            "attachment; filename=\"qrcode_" + qrCodeId + ".png\"")
                    .body(resource);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/view/{qrCodeId}")
    public ResponseEntity<Resource> viewQrCode(@PathVariable String qrCodeId) {
        try {
            File qrCodeFile = qrCodeService.getQrCodeFile(qrCodeId);
            
            Resource resource = new FileSystemResource(qrCodeFile);
            
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                    .body(resource);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/info/{qrCodeId}")
    public ResponseEntity<?> getQrCodeInfo(@PathVariable String qrCodeId) {
        Optional<QrCodeMetadata> qrCodeMetadata = qrCodeService.getQrCodeMetadata(qrCodeId);
        
        if (qrCodeMetadata.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(qrCodeMetadata.get());
    }

    @DeleteMapping("/{qrCodeId}")
    public ResponseEntity<?> deleteQrCode(@PathVariable String qrCodeId) {
        try {
            qrCodeService.deleteQrCode(qrCodeId);
            return ResponseEntity.ok().body("QR Code supprimé avec succès");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de la suppression: " + e.getMessage());
        }
    }
}

