package will.dev.qrcodeApp.mapper;

import org.springframework.stereotype.Component;
import will.dev.qrcodeApp.dto.PdfMetadataDto;
import will.dev.qrcodeApp.entity.PdfMetadata;

@Component
public class PdfMetadataMapper {

    public PdfMetadataDto pdfMetadataToPdfMetadataDto(PdfMetadata pdfMetadata) {
        if (pdfMetadata == null) {
            return null;
        }

        PdfMetadataDto dto = new PdfMetadataDto();
        dto.setId(pdfMetadata.getId());
        dto.setUniqueId(pdfMetadata.getUniqueId());
        dto.setOriginalFilename(pdfMetadata.getOriginalFilename());
        dto.setFilePath(pdfMetadata.getFilePath());
        dto.setFileSize(pdfMetadata.getFileSize());
        dto.setUploadDate(pdfMetadata.getUploadDate());
        dto.setContentType(pdfMetadata.getContentType());

        // relation User
        if (pdfMetadata.getUser() != null) {
            dto.setUserId(pdfMetadata.getUser().getId());
        }

        return dto;
    }

    public PdfMetadata pdfMetadataDtoToPdfMetadata(PdfMetadataDto dto) {
        if (dto == null) {
            return null;
        }

        PdfMetadata entity = new PdfMetadata();
        entity.setId(dto.getId());
        entity.setUniqueId(dto.getUniqueId());
        entity.setOriginalFilename(dto.getOriginalFilename());
        entity.setFilePath(dto.getFilePath());
        entity.setFileSize(dto.getFileSize());
        entity.setUploadDate(dto.getUploadDate());
        entity.setContentType(dto.getContentType());

        // ⚠ Comme dans MapStruct : user et qrCodes sont ignorés
        // Ils devront être liés dans ton service avant save()

        return entity;
    }
}



