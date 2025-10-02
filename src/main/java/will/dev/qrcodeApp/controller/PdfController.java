package will.dev.qrcodeApp.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import will.dev.qrcodeApp.dto.PdfMetadataDto;
import will.dev.qrcodeApp.entity.PdfMetadata;
import will.dev.qrcodeApp.entity.User;
import will.dev.qrcodeApp.mapper.PdfMetadataMapper;
import will.dev.qrcodeApp.service.PdfService;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/pdf")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PdfController {

    private final PdfService pdfService;
    private final PdfMetadataMapper pdfMetadataMapper;

    @PostMapping("/upload")
    @PreAuthorize("hasAnyAuthority(\'USER\', \'MANAGER\', \'ADMIN\')")
    public ResponseEntity<?> uploadPdf(@AuthenticationPrincipal User user,
                                       @RequestParam("file") MultipartFile file) {
        try {
            PdfMetadata pdf = pdfService.uploadPdf(user, file);
            return ResponseEntity.ok(pdfMetadataMapper.pdfMetadataToPdfMetadataDto(pdf));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("❌ Erreur serveur : " + e.getMessage());
        }
    }

    @GetMapping("/view/{pdfUniqueId}")
    public ResponseEntity<Void> viewPdf(@PathVariable String pdfUniqueId) {
        Optional<PdfMetadata> pdfMetadata = pdfService.getPdfMetadata(pdfUniqueId);

        if (pdfMetadata.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // Soit tu rediriges vers l’URL Cloudinary
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(pdfMetadata.get().getFilePath()))
                .build();
    }

    @GetMapping("/download/{pdfUniqueId}")
    public ResponseEntity<Void> downloadPdf(@PathVariable String pdfUniqueId) {
        Optional<PdfMetadata> pdfMetadata = pdfService.getPdfMetadata(pdfUniqueId);

        if (pdfMetadata.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // Ajout du paramètre download=true pour forcer Cloudinary à déclencher un téléchargement
        String downloadUrl = pdfMetadata.get().getFilePath() + "?download=true";

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(downloadUrl))
                .build();
    }


    @GetMapping("/info/{pdfUniqueId}")
    @PreAuthorize("hasAnyAuthority(\'USER\', \'MANAGER\', \'ADMIN\')")
    public ResponseEntity<PdfMetadataDto> getPdfInfo(@PathVariable String pdfUniqueId) {
        Optional<PdfMetadata> pdfMetadata = pdfService.getPdfMetadata(pdfUniqueId);

        if (pdfMetadata.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(pdfMetadataMapper.pdfMetadataToPdfMetadataDto(pdfMetadata.get()));
    }

    @GetMapping("/text/{pdfUniqueId}")
    @PreAuthorize("hasAnyAuthority(\'USER\', \'MANAGER\', \'ADMIN\')")
    public ResponseEntity<String> extractTextFromPdf(@PathVariable String pdfUniqueId) {
        try {
            String text = pdfService.extractTextFromPdf(pdfUniqueId);
            return ResponseEntity.ok().body(text);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de l'extraction du texte: " + e.getMessage());
        }
    }

    @DeleteMapping("/{pdfUniqueId}")
    @PreAuthorize("hasAnyAuthority(\'ADMIN\', \'MANAGER\') or (hasAuthority(\'USER\') and @pdfService.getPdfMetadata(#pdfUniqueId).get().getUser().getId() == authentication.principal.id)")
    public ResponseEntity<String> deletePdf(@AuthenticationPrincipal User user, @PathVariable String pdfUniqueId) {
        try {
            pdfService.deletePdf(user, pdfUniqueId);
            return ResponseEntity.ok().body("PDF supprimé avec succès");
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de la suppression: " + e.getMessage());
        }
    }

    @GetMapping("/user-pdfs")
    @PreAuthorize("hasAnyAuthority(\'USER\', \'MANAGER\', \'ADMIN\')")
    public ResponseEntity<List<PdfMetadataDto>> getUserPdfs(@AuthenticationPrincipal User user) {
        List<PdfMetadata> pdfs = pdfService.getUserPdfs(user);
        return ResponseEntity.ok(pdfs.stream().map(pdfMetadataMapper::pdfMetadataToPdfMetadataDto).collect(Collectors.toList()));
    }
}


