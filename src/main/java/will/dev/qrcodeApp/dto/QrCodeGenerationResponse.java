package will.dev.qrcodeApp.dto;

import java.time.LocalDateTime;

public class QrCodeGenerationResponse {
    private String qrCodeId;
    private String pdfId;
    private String qrContent;
    private String downloadUrl;
    private LocalDateTime generationDate;
    private String message;

    public QrCodeGenerationResponse() {}

    public QrCodeGenerationResponse(String qrCodeId, String pdfId, String qrContent, String downloadUrl, LocalDateTime generationDate, String message) {
        this.qrCodeId = qrCodeId;
        this.pdfId = pdfId;
        this.qrContent = qrContent;
        this.downloadUrl = downloadUrl;
        this.generationDate = generationDate;
        this.message = message;
    }

    // Getters et Setters
    public String getQrCodeId() {
        return qrCodeId;
    }

    public void setQrCodeId(String qrCodeId) {
        this.qrCodeId = qrCodeId;
    }

    public String getPdfId() {
        return pdfId;
    }

    public void setPdfId(String pdfId) {
        this.pdfId = pdfId;
    }

    public String getQrContent() {
        return qrContent;
    }

    public void setQrContent(String qrContent) {
        this.qrContent = qrContent;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public LocalDateTime getGenerationDate() {
        return generationDate;
    }

    public void setGenerationDate(LocalDateTime generationDate) {
        this.generationDate = generationDate;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

