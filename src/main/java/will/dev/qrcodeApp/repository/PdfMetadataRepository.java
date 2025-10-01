package will.dev.qrcodeApp.repository;

import will.dev.qrcodeApp.entity.PdfMetadata;
import will.dev.qrcodeApp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PdfMetadataRepository extends JpaRepository<PdfMetadata, Long> {
    
    Optional<PdfMetadata> findByUniqueId(String uniqueId);
    
    boolean existsByUniqueId(String uniqueId);
    
    List<PdfMetadata> findByUserOrderByUploadDateDesc(User user);
    
    List<PdfMetadata> findByOriginalFilenameContainingIgnoreCase(String filename);
    
    @Query("SELECT p FROM PdfMetadata p WHERE p.user.id = :userId ORDER BY p.uploadDate DESC")
    List<PdfMetadata> findByUserIdOrderByUploadDateDesc(@Param("userId") Long userId);
    
    @Query("SELECT COUNT(p) FROM PdfMetadata p WHERE p.user = :user")
    Long countByUser(@Param("user") User user);
    
    void deleteByUniqueId(String uniqueId);

    @Query("SELECT COUNT(p) > 0 FROM PdfMetadata p WHERE p.uniqueId = :uniqueId AND p.originalFilename = :uploadedFileName")
    boolean existsByUniqueIdAndOriginalFilename(@Param("uniqueId") String uniqueId,
                                                @Param("uploadedFileName") String uploadedFileName);

}

