package will.dev.qrcodeApp.repository;

import will.dev.qrcodeApp.entity.QrCodeMetadata;
import will.dev.qrcodeApp.entity.User;
import will.dev.qrcodeApp.entity.PdfMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QrCodeMetadataRepository extends JpaRepository<QrCodeMetadata, Long> {
    
    Optional<QrCodeMetadata> findByUniqueId(String uniqueId);
    
    Optional<QrCodeMetadata> findByPdfId(String pdfId);
    
    boolean existsByUniqueId(String uniqueId);
    
    boolean existsByPdfId(String pdfId);
    
    List<QrCodeMetadata> findByUserOrderByGenerationDateDesc(User user);
    
    List<QrCodeMetadata> findByPdfMetadataOrderByGenerationDateDesc(PdfMetadata pdfMetadata);
    
    List<QrCodeMetadata> findAllByPdfId(String pdfId);
    
    @Query("SELECT q FROM QrCodeMetadata q WHERE q.user.id = :userId ORDER BY q.generationDate DESC")
    List<QrCodeMetadata> findByUserIdOrderByGenerationDateDesc(@Param("userId") Long userId);
    
    @Query("SELECT COUNT(q) FROM QrCodeMetadata q WHERE q.user = :user")
    Long countByUser(@Param("user") User user);
    
    @Query("SELECT q FROM QrCodeMetadata q WHERE q.qrContent LIKE %:content%")
    List<QrCodeMetadata> findByQrContentContaining(@Param("content") String content);
    
    void deleteByUniqueId(String uniqueId);

    Optional<QrCodeMetadata> findByPdfMetadataAndUser(PdfMetadata pdfMetadata, User user);
}

