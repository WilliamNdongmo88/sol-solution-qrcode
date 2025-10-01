package will.dev.qrcodeApp.dto;

import java.time.LocalDateTime;

public class PdfUploadResponse {
    private String pdfId;
    private String originalFilename;
    private Long fileSize;
    private LocalDateTime uploadDate;
    private String message;

    public PdfUploadResponse() {}

    public PdfUploadResponse(String pdfId, String originalFilename, Long fileSize, LocalDateTime uploadDate, String message) {
        this.pdfId = pdfId;
        this.originalFilename = originalFilename;
        this.fileSize = fileSize;
        this.uploadDate = uploadDate;
        this.message = message;
    }

    // Getters et Setters
    public String getPdfId() {
        return pdfId;
    }

    public void setPdfId(String pdfId) {
        this.pdfId = pdfId;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public LocalDateTime getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(LocalDateTime uploadDate) {
        this.uploadDate = uploadDate;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

