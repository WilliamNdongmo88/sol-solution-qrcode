package will.dev.qrcodeApp.mapper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import will.dev.qrcodeApp.dto.QrCodeMetadataDto;
import will.dev.qrcodeApp.entity.QrCodeMetadata;

@Component
public class QrCodeMetadataMapper {

    @Value("${BASE_URL}")
    private String baseUrl;

    public QrCodeMetadataDto qrCodeMetadataToQrCodeMetadataDto(QrCodeMetadata qrCodeMetadata) {
        if (qrCodeMetadata == null) {
            return null;
        }

        QrCodeMetadataDto dto = new QrCodeMetadataDto();
        dto.setId(qrCodeMetadata.getId());
        dto.setQrCodeId(qrCodeMetadata.getUniqueId());
        dto.setDownloadUrl(baseUrl + "/api/qrcode/download/");
        dto.setFilePath(qrCodeMetadata.getFilePath());
        dto.setPdfId(qrCodeMetadata.getPdfId());
        dto.setQrContent(qrCodeMetadata.getQrContent());
        dto.setGenerationDate(qrCodeMetadata.getGenerationDate());
        dto.setImageFormat(qrCodeMetadata.getImageFormat());
        dto.setQrName(qrCodeMetadata.getQrName() + "."+qrCodeMetadata.getImageFormat());
        dto.setImageSize(qrCodeMetadata.getImageSize());

        // mapping des relations
        if (qrCodeMetadata.getUser() != null) {
            dto.setUserId(qrCodeMetadata.getUser().getId());
            dto.setUserName(qrCodeMetadata.getUser().getUsername());
        }
        if (qrCodeMetadata.getPdfMetadata() != null) {
            dto.setPdfMetadataId(qrCodeMetadata.getPdfMetadata().getId());
            dto.setPdfMetadataName(qrCodeMetadata.getPdfMetadata().getOriginalFilename());
        }

        return dto;
    }

    public QrCodeMetadata qrCodeMetadataDtoToQrCodeMetadata(QrCodeMetadataDto dto) {
        if (dto == null) {
            return null;
        }

        QrCodeMetadata entity = new QrCodeMetadata();
        entity.setId(dto.getId());
        entity.setUniqueId(dto.getQrCodeId());
        entity.setFilePath(dto.getFilePath());
        entity.setPdfId(dto.getPdfId());
        entity.setQrContent(dto.getQrContent());
        entity.setGenerationDate(dto.getGenerationDate());
        entity.setImageFormat(dto.getImageFormat());
        entity.setImageSize(dto.getImageSize());

        // ⚠ On ignore user et pdfMetadata (comme dans ton MapStruct avec `ignore = true`)
        // Ils devront être liés dans le service avant persist()

        return entity;
    }
}
