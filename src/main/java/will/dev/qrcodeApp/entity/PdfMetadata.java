package will.dev.qrcodeApp.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "pdf_metadata")
public class PdfMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "unique_id", unique = true, nullable = false)
    private String uniqueId;

    @Column(name = "original_filename", nullable = false)
    private String originalFilename;

    @Column(name = "file_path", nullable = false)
    private String filePath;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "upload_date")
    private LocalDateTime uploadDate;

    @Column(name = "content_type")
    private String contentType;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
    @OneToMany(mappedBy = "pdfMetadata", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<QrCodeMetadata> qrCodes;

    // Constructeurs
    public PdfMetadata() {
        this.uploadDate = LocalDateTime.now();
    }

    public PdfMetadata(String uniqueId, String originalFilename, String filePath, Long fileSize, String contentType, User user) {
        this();
        this.uniqueId = uniqueId;
        this.originalFilename = originalFilename;
        this.filePath = filePath;
        this.fileSize = fileSize;
        this.contentType = contentType;
        this.user = user;
    }

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
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

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public List<QrCodeMetadata> getQrCodes() {
        return qrCodes;
    }
    
    public void setQrCodes(List<QrCodeMetadata> qrCodes) {
        this.qrCodes = qrCodes;
    }
}

