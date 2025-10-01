package will.dev.qrcodeApp.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PdfMetadataDto {
    private Long id;
    private String uniqueId;
    private String originalFilename;
    private String filePath;
    private Long fileSize;
    private LocalDateTime uploadDate;
    private String contentType;
    private Long userId;
}


