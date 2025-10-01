package will.dev.qrcodeApp.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class QrCodeMetadataDto {
    private Long id;
    private String qrName;
    private String qrCodeId;
    private String downloadUrl;
    private String filePath;
    private String pdfId;
    private String qrContent;
    private LocalDateTime generationDate;
    private String imageFormat;
    private Integer imageSize;
    private Long userId;
    private  String userName;
    private Long pdfMetadataId;
    private String pdfMetadataName;
}


